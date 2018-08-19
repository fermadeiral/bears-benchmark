/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.enmasse.systemtest.standard;

import io.enmasse.systemtest.*;
import io.enmasse.systemtest.amqp.AmqpClient;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class QueueTest extends StandardTestBase {

    @Test
    public void testColocatedQueues() throws Exception {
        Destination q1 = Destination.queue("queue1", Optional.of("pooled-inmemory"));
        Destination q2 = Destination.queue("queue2", Optional.of("pooled-inmemory"));
        Destination q3 = Destination.queue("queue3", Optional.of("pooled-inmemory"));
        setAddresses(q1, q2, q3);

        AmqpClient client = amqpClientFactory.createQueueClient();
        runQueueTest(client, q1);
        runQueueTest(client, q2);
        runQueueTest(client, q3);
    }


    public void testInmemoryQueues() throws Exception {
        Destination q1 = Destination.queue("inMemoryQueue1", Optional.of("inmemory"));
        Destination q2 = Destination.queue("inMemoryQueue2", Optional.of("inmemory"));

        setAddresses(q1, q2);

        AmqpClient client = amqpClientFactory.createQueueClient();
        runQueueTest(client, q1);
        runQueueTest(client, q2);
    }


    public void testPersistedQueues() throws Exception {
        Destination q1 = Destination.queue("persistedQueue1", Optional.of("persisted"));
        Destination q2 = Destination.queue("persistedQueue2", Optional.of("persisted"));

        setAddresses(q1, q2);

        AmqpClient client = amqpClientFactory.createQueueClient();
        runQueueTest(client, q1);
        runQueueTest(client, q2);
    }


    public void testPooledPersistedQueues() throws Exception {
        Destination q1 = Destination.queue("pooledPersistedQueue1", Optional.of("pooled-persisted"));
        Destination q2 = Destination.queue("pooledPersistedQueue2", Optional.of("pooled-persisted"));

        setAddresses(q1, q2);

        AmqpClient client = amqpClientFactory.createQueueClient();
        runQueueTest(client, q1);
        runQueueTest(client, q2);
    }

    @Test
    public void testRestApi() throws Exception {
        List<String> queues = Arrays.asList("queue1", "queue2");
        Destination q1 = Destination.queue(queues.get(0), Optional.of("pooled-inmemory"));
        Destination q2 = Destination.queue(queues.get(1), Optional.of("pooled-inmemory"));

        runRestApiTest(queues, q1, q2);
    }

    @Test
    public void testCreateDeleteQueue() throws Exception {
        List<String> queues = IntStream.range(0, 16).mapToObj(i -> "queue-create-delete-" + i).collect(Collectors.toList());
        Destination destExtra = Destination.queue("ext-queue", Optional.of("pooled-inmemory"));

        List<Destination> addresses = new ArrayList<>();
        queues.forEach(queue -> addresses.add(Destination.queue(queue, Optional.of("pooled-inmemory"))));

        AmqpClient client = amqpClientFactory.createQueueClient();
        for (Destination address : addresses) {
            setAddresses(address, destExtra);
            Thread.sleep(20_000);

            //runQueueTest(client, address, 1); //TODO! commented due to issue #429

            deleteAddresses(address);
            Future<List<String>> response = getAddresses(Optional.empty());
            assertThat(response.get(20, TimeUnit.SECONDS), is(Arrays.asList(destExtra.getAddress())));
            deleteAddresses(destExtra);
            response = getAddresses(Optional.empty());
            assertThat(response.get(20, TimeUnit.SECONDS), is(java.util.Collections.emptyList()));
            Thread.sleep(20_000);
        }
    }

    @Test
    public void testMessagePriorities() throws Exception {
        Destination dest = Destination.queue("messagePrioritiesQueue");
        setAddresses(dest);

        AmqpClient client = amqpClientFactory.createQueueClient();
        Thread.sleep(30_000);

        int msgsCount = 1024;
        List<Message> listOfMessages = new ArrayList<>();
        for (int i = 0; i < msgsCount; i++) {
            Message msg = Message.Factory.create();
            msg.setAddress(dest.getAddress());
            msg.setBody(new AmqpValue(dest.getAddress()));
            msg.setSubject("subject");
            msg.setPriority((short) (i % 10));
            listOfMessages.add(msg);
        }

        Future<Integer> sent = client.sendMessages(dest.getAddress(),
                listOfMessages.toArray(new Message[listOfMessages.size()]));
        assertThat(sent.get(1, TimeUnit.MINUTES), is(msgsCount));

        Future<List<Message>> received = client.recvMessages(dest.getAddress(), msgsCount);
        assertThat(received.get(1, TimeUnit.MINUTES).size(), is(msgsCount));

        int sub = 1;
        for (Message m : received.get()) {
            for (Message mSub : received.get().subList(sub, received.get().size())) {
                assertTrue(m.getPriority() >= mSub.getPriority());
            }
            sub++;
        }
    }

    public void testScaledown() throws Exception {
        Destination dest = Destination.queue("scalequeue");
        setAddresses(dest);
        scale(dest, 4);
        AmqpClient client = amqpClientFactory.createQueueClient();
        List<Future<Integer>> sent = Arrays.asList(
                client.sendMessages(dest.getAddress(), TestUtils.generateMessages("foo", 1000)),
                client.sendMessages(dest.getAddress(), TestUtils.generateMessages("bar", 1000)),
                client.sendMessages(dest.getAddress(), TestUtils.generateMessages("baz", 1000)),
                client.sendMessages(dest.getAddress(), TestUtils.generateMessages("quux", 1000)));

        assertThat(sent.get(0).get(1, TimeUnit.MINUTES), is(1000));
        assertThat(sent.get(1).get(1, TimeUnit.MINUTES), is(1000));
        assertThat(sent.get(2).get(1, TimeUnit.MINUTES), is(1000));
        assertThat(sent.get(3).get(1, TimeUnit.MINUTES), is(1000));

        Future<List<Message>> received = client.recvMessages(dest.getAddress(), 500);
        assertThat(received.get(1, TimeUnit.MINUTES).size(), is(500));

        scale(dest, 1);

        received = client.recvMessages(dest.getAddress(), 3500);

        assertThat(received.get(1, TimeUnit.MINUTES).size(), is(3500));
    }

    public static void runQueueTest(AmqpClient client, Destination dest) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        runQueueTest(client, dest, 1024);
    }

    public static void runQueueTest(AmqpClient client, Destination dest, int countMessages) throws InterruptedException, TimeoutException, ExecutionException, IOException {
        List<String> msgs = TestUtils.generateMessages(countMessages);
        Count<Message> predicate = new Count<>(msgs.size());
        Future<Integer> numSent = client.sendMessages(dest.getAddress(), msgs, predicate);

        assertNotNull(numSent);
        int actual = 0;
        try {
            actual = numSent.get(1, TimeUnit.MINUTES);
        } catch (TimeoutException t) {
            fail("Sending messages timed out after sending " + predicate.actual());
        }
        assertThat(actual, is(msgs.size()));

        predicate = new Count<>(msgs.size());
        Future<List<Message>> received = client.recvMessages(dest.getAddress(), predicate);
        actual = 0;
        try {
            actual = received.get(1, TimeUnit.MINUTES).size();
        } catch (TimeoutException t) {
            fail("Receiving messages timed out after " + predicate.actual() + " msgs received");
        }

        assertThat(actual, is(msgs.size()));
    }
}

