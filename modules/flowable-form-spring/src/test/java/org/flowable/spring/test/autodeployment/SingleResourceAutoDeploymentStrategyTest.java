/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.spring.test.autodeployment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.form.spring.autodeployment.SingleResourceAutoDeploymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;

/**
 * @author Tiese Barrell
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class SingleResourceAutoDeploymentStrategyTest extends AbstractAutoDeploymentStrategyTest {

    private SingleResourceAutoDeploymentStrategy classUnderTest;

    @BeforeEach
    @Override
    public void before() throws Exception {
        super.before();
        classUnderTest = new SingleResourceAutoDeploymentStrategy();
        assertNotNull(classUnderTest);
    }

    @Test
    public void testHandlesMode() {
        assertTrue(classUnderTest.handlesMode(SingleResourceAutoDeploymentStrategy.DEPLOYMENT_MODE));
        assertFalse(classUnderTest.handlesMode("other-mode"));
        assertFalse(classUnderTest.handlesMode(null));
    }

    @Test
    public void testDeployResources() {
        final Resource[] resources = new Resource[] { resourceMock1, resourceMock2, resourceMock3 };
        classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock);

        verify(repositoryServiceMock, times(3)).createDeployment();
        verify(deploymentBuilderMock, times(3)).enableDuplicateFiltering();
        verify(deploymentBuilderMock, times(1)).name(resourceName1);
        verify(deploymentBuilderMock, times(1)).name(resourceName2);
        verify(deploymentBuilderMock, times(1)).name(resourceName3);
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName1), isA(InputStream.class));
        verify(deploymentBuilderMock, times(1)).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, times(3)).deploy();
    }

    @Test
    public void testDeployResourcesNoResources() {
        final Resource[] resources = new Resource[] {};
        classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock);

        verify(repositoryServiceMock, never()).createDeployment();
        verify(deploymentBuilderMock, never()).enableDuplicateFiltering();
        verify(deploymentBuilderMock, never()).name(deploymentNameHint);
        verify(deploymentBuilderMock, never()).addInputStream(isA(String.class), isA(InputStream.class));
        verify(deploymentBuilderMock, never()).addInputStream(eq(resourceName2), isA(InputStream.class));
        verify(deploymentBuilderMock, never()).deploy();
    }

    @Test
    public void testDeployResourcesIOExceptionYieldsFlowableException() throws Exception {
        IOException ioException = new IOException("IO message");
        when(resourceMock3.getInputStream()).thenThrow(ioException);

        final Resource[] resources = new Resource[] { resourceMock3 };
        assertThatThrownBy(() ->
            classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock))
            .isInstanceOf(FlowableException.class)
            .hasMessage("couldn't auto deploy resource 'resourceMock3': IO message")
            .hasCause(ioException);
    }

}