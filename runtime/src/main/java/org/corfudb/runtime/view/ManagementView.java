package org.corfudb.runtime.view;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.corfudb.protocols.wireprotocol.orchestrator.CreateWorkflowResponse;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.clients.ManagementClient;
import org.corfudb.runtime.exceptions.NetworkException;
import org.corfudb.runtime.exceptions.WorkflowException;
import org.corfudb.runtime.exceptions.WorkflowResultUnknownException;
import org.corfudb.util.Sleep;

import javax.annotation.Nonnull;

/**
 * A view of the Management Service to manage reconfigurations of the Corfu Cluster.
 * <p>
 * <p>Created by zlokhandwala on 11/20/17.</p>
 */
@Slf4j
public class ManagementView extends AbstractView {

    public ManagementView(@NonNull CorfuRuntime runtime) {
        super(runtime);
    }

    /**
     * Determines the orchestrator endpoint from the provided layout.
     *
     * @param layout Latest layout.
     * @return Returns the orchestrator endpoint.
     */
    private String getOrchestratorEndpoint(Layout layout) {
        List<String> activeLayoutServers = layout.getLayoutServers().stream()
                .filter(s -> !layout.getUnresponsiveServers().contains(s))
                .collect(Collectors.toList());
        return activeLayoutServers.get(0);
    }

    /**
     * @param workflow   the workflow id to poll
     * @param client     a client that is connected to the orchestrator that is
     *                   running the workflow
     * @param timeout    the total time to wait for the workflow to complete
     * @param pollPeriod the poll period to query the completion of the workflow
     * @throws NetworkException if the client disconnects
     * @throws TimeoutException if the workflow doesn't complete withint the timout
     *                          period
     */
    void waitForWorkflow(UUID workflow, ManagementClient client, Duration timeout,
                         Duration pollPeriod) throws NetworkException, TimeoutException {
        long tries = timeout.getSeconds() / pollPeriod.getSeconds();
        for (long x = 0; x < tries; x++) {
            if (!client.queryRequest(workflow).isActive()) {
                return;
            }
            Sleep.sleepUninterruptibly(pollPeriod);
            log.info("waitForWorkflow: waiting for {} on try {}", workflow, x);
        }
        log.debug("waitForWorkflow: workflow {} timeout", workflow);
        throw new TimeoutException();
    }

    /**
     * Remove a node from the cluster.
     *
     * @param endpointToRemove Endpoint of the node to be removed from the cluster.
     * @param retry            the number of times to retry a workflow if it fails
     * @param timeout          total time to wait before the workflow times out
     * @param pollPeriod       the poll interval to check whether a workflow completed or not
     * @throws WorkflowResultUnknownException when the side affect of the operation
     *                                        can't be determined
     * @throws WorkflowException              when the remove operation fails
     */
    public void removeNode(@Nonnull String endpointToRemove, int retry,
                           @Nonnull Duration timeout, @Nonnull Duration pollPeriod) {

        for (int x = 0; x < retry; x++) {
            try {
                runtime.invalidateLayout();
                Layout layout = runtime.getLayoutView().getLayout();
                List<String> layoutServers = new ArrayList(layout.getLayoutServers());
                layoutServers.remove(endpointToRemove);
                if (layoutServers.isEmpty()) {
                    throw new WorkflowException("Can't remove node from a single node cluster");
                }
                String orchestratorEndpoint = getOrchestratorEndpoint(layout);
                ManagementClient client = runtime.getRouter(orchestratorEndpoint)
                        .getClient(ManagementClient.class);
                CreateWorkflowResponse resp = client.removeNode(endpointToRemove);
                log.info("removeNode: requested to remove {} on orchestrator {}, layout {}",
                        endpointToRemove, orchestratorEndpoint, layout);

                waitForWorkflow(resp.getWorkflowId(), client, timeout, pollPeriod);

                for (int y = 0; y < runtime.getParameters().getInvalidateRetry(); y++) {
                    runtime.invalidateLayout();
                    if (!runtime.getLayoutView().getLayout()
                            .getAllServers().contains(endpointToRemove)) {
                        // Node removed successfully
                        log.info("removeNode: Successfully removed {}", endpointToRemove);
                        return;
                    }
                }
            } catch (NetworkException | TimeoutException e) {
                log.warn("removeNode: Exception while trying to remove node {} on try ",
                        endpointToRemove, x, e);
                continue;
            }
            log.warn("removeNode: Attempting to remove {} on try {}", endpointToRemove, x);
        }

        throw new WorkflowResultUnknownException();
    }

    /**
     * Add a new node to the existing cluster.
     *
     * @param endpointToAdd Endpoint of the new node to be added to the cluster.
     * @param retry         the number of times to retry a workflow if it fails
     * @param timeout       total time to wait before the workflow times out
     * @param pollPeriod    the poll interval to check whether a workflow completed or not
     * @throws WorkflowResultUnknownException when the side affect of the operation
     *                                        can't be determined
     */
    public void addNode(@Nonnull String endpointToAdd, int retry,
                        @Nonnull Duration timeout, @Nonnull Duration pollPeriod) {

        for (int x = 0; x < retry; x++) {
            try {
                runtime.invalidateLayout();
                Layout layout = runtime.getLayoutView().getLayout();
                String orchestratorEndpoint = getOrchestratorEndpoint(layout);
                ManagementClient client = runtime.getRouter(orchestratorEndpoint)
                        .getClient(ManagementClient.class);

                CreateWorkflowResponse resp = client.addNodeRequest(endpointToAdd);
                log.info("addNode: requested to add {} on orchestrator {}, layout {}", endpointToAdd,
                        orchestratorEndpoint, layout);

                waitForWorkflow(resp.getWorkflowId(), client, timeout, pollPeriod);

                for (int y = 0; y < runtime.getParameters().getInvalidateRetry(); y++) {
                    runtime.invalidateLayout();
                    if (runtime.getLayoutView().getLayout().getAllServers().contains(endpointToAdd)
                            && layout.getSegmentsForEndpoint(endpointToAdd).size() == 1) {
                        log.info("addNode: Successfully added {}", endpointToAdd);
                        return;
                    }
                }
            } catch (NetworkException | TimeoutException e) {
                log.warn("addNode: Exception while trying to add node {} on try ",
                        endpointToAdd, x, e);
                continue;
            }
            log.warn("addNode: Attempting to add {} on try {}", endpointToAdd, x);
        }

        throw new WorkflowResultUnknownException();
    }

    /**
     * Heal an unresponsive node.
     *
     * @param endpointToHeal Endpoint of the new node to be healed in the cluster.
     * @param retry          the number of times to retry a workflow if it fails
     * @param timeout        total time to wait before the workflow times out
     * @param pollPeriod     the poll interval to check whether a workflow completed or not
     * @throws WorkflowResultUnknownException when the side affect of the operation
     *                                        can't be determined
     */
    public void healNode(@Nonnull String endpointToHeal, int retry, @Nonnull Duration timeout,
                         @Nonnull Duration pollPeriod) {

        for (int retryAttempt = 0; retryAttempt < retry; retryAttempt++) {
            try {
                runtime.invalidateLayout();
                Layout layout = runtime.getLayoutView().getLayout();
                String orchestratorEndpoint = getOrchestratorEndpoint(layout);
                ManagementClient client = runtime.getRouter(orchestratorEndpoint)
                        .getClient(ManagementClient.class);

                CreateWorkflowResponse resp = client
                        .healNodeRequest(endpointToHeal, true, true, true, 0);
                log.info("healNode: requested to add {} on orchestrator {}, layout {}",
                        endpointToHeal, orchestratorEndpoint, layout);

                waitForWorkflow(resp.getWorkflowId(), client, timeout, pollPeriod);

                for (int y = 0; y < runtime.getParameters().getInvalidateRetry(); y++) {
                    runtime.invalidateLayout();
                    if (runtime.getLayoutView().getLayout().getAllServers().contains(endpointToHeal)
                            && layout.getSegmentsForEndpoint(endpointToHeal).size() == 1) {
                        log.info("healNode: Successfully added {}", endpointToHeal);
                        return;
                    }
                }
            } catch (NetworkException | TimeoutException e) {
                log.warn("healNode: Exception while trying to add node {} on try ",
                        endpointToHeal, retryAttempt, e);
                continue;
            }
            log.warn("healNode: Attempting to add {} on try {}", endpointToHeal, retryAttempt);
        }

        throw new WorkflowResultUnknownException();
    }
}
