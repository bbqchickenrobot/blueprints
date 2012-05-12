package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.pgm.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class KeyIndexableGraphTestSuite extends TestSuite {

    public KeyIndexableGraphTestSuite() {
    }

    public KeyIndexableGraphTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testAutoIndexKeyManagement() {
        KeyIndexableGraph graph = (KeyIndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexKeyIndex) {
            assertEquals(graph.getIndexedKeys(Vertex.class).size(), 0);
            this.stopWatch();
            graph.createKeyIndex("name", Vertex.class);
            graph.createKeyIndex("location", Vertex.class);
            printPerformance(graph.toString(), 2, "automatic index keys added", this.stopWatch());
            assertEquals(graph.getIndexedKeys(Vertex.class).size(), 2);
            assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
            assertTrue(graph.getIndexedKeys(Vertex.class).contains("location"));
        }
        if (graph.getFeatures().supportsEdgeKeyIndex) {
            assertEquals(graph.getIndexedKeys(Edge.class).size(), 0);
            this.stopWatch();
            graph.createKeyIndex("weight", Edge.class);
            graph.createKeyIndex("since", Edge.class);
            printPerformance(graph.toString(), 2, "automatic index keys added", this.stopWatch());
            assertEquals(graph.getIndexedKeys(Edge.class).size(), 2);
            assertTrue(graph.getIndexedKeys(Edge.class).contains("weight"));
            assertTrue(graph.getIndexedKeys(Edge.class).contains("since"));
        }
        graph.shutdown();
        if (graph.getFeatures().isPersistent) {
            graph = (KeyIndexableGraph) graphTest.generateGraph();
            if (graph.getFeatures().supportsVertexKeyIndex) {
                assertEquals(graph.getIndexedKeys(Vertex.class).size(), 2);
                assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
                assertTrue(graph.getIndexedKeys(Vertex.class).contains("location"));
                graph.dropKeyIndex("name", Vertex.class);
            }
            if (graph.getFeatures().supportsEdgeKeyIndex) {
                assertEquals(graph.getIndexedKeys(Edge.class).size(), 2);
                assertTrue(graph.getIndexedKeys(Edge.class).contains("weight"));
                assertTrue(graph.getIndexedKeys(Edge.class).contains("since"));
                graph.dropKeyIndex("weight", Edge.class);
            }
            graph.shutdown();

            graph = (KeyIndexableGraph) graphTest.generateGraph();
            if (graph.getFeatures().supportsVertexKeyIndex) {
                assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
                assertTrue(graph.getIndexedKeys(Vertex.class).contains("location"));
                graph.dropKeyIndex("location", Vertex.class);
            }
            if (graph.getFeatures().supportsEdgeKeyIndex) {
                assertEquals(graph.getIndexedKeys(Edge.class).size(), 1);
                assertTrue(graph.getIndexedKeys(Edge.class).contains("since"));
                graph.dropKeyIndex("since", Edge.class);
            }
            graph.shutdown();
            graph = (KeyIndexableGraph) graphTest.generateGraph();
            if (graph.getFeatures().supportsVertexKeyIndex) {
                assertEquals(graph.getIndexedKeys(Vertex.class).size(), 0);
            }
            if (graph.getFeatures().supportsEdgeKeyIndex) {
                assertEquals(graph.getIndexedKeys(Edge.class).size(), 0);
            }
            graph.shutdown();

        }

    }

    public void testGettingVerticesAndEdgesWithKeyValue() {
        KeyIndexableGraph graph = (KeyIndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration && graph.getFeatures().supportsVertexKeyIndex) {
            graph.createKeyIndex("name", Vertex.class);
            assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
            assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
            Vertex v1 = graph.addVertex(null);
            v1.setProperty("name", "marko");
            v1.setProperty("location", "everywhere");
            Vertex v2 = graph.addVertex(null);
            v2.setProperty("name", "stephen");
            v2.setProperty("location", "everywhere");

            assertEquals(count(graph.getVertices("location", "everywhere")), 2);
            assertEquals(count(graph.getVertices("name", "marko")), 1);
            assertEquals(count(graph.getVertices("name", "stephen")), 1);
            assertEquals(graph.getVertices("name", "marko").iterator().next(), v1);
            assertEquals(graph.getVertices("name", "stephen").iterator().next(), v2);
        }

        if (graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsEdgeKeyIndex) {
            graph.createKeyIndex("location", Edge.class);
            assertEquals(graph.getIndexedKeys(Edge.class).size(), 1);
            assertTrue(graph.getIndexedKeys(Edge.class).contains("location"));

            Edge e1 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
            e1.setProperty("name", "marko");
            e1.setProperty("location", "everywhere");
            Edge e2 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
            e2.setProperty("name", "stephen");
            e2.setProperty("location", "everywhere");

            assertEquals(count(graph.getEdges("location", "everywhere")), 2);
            assertEquals(count(graph.getEdges("name", "marko")), 1);
            assertEquals(count(graph.getEdges("name", "stephen")), 1);
            assertEquals(graph.getEdges("name", "marko").iterator().next(), e1);
            assertEquals(graph.getEdges("name", "stephen").iterator().next(), e2);
        }
        graph.shutdown();
    }

    public void testNoConcurrentModificationException() {
        KeyIndexableGraph graph = (KeyIndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeKeyIndex) {
            graph.createKeyIndex("key", Edge.class);
            for (int i = 0; i < 25; i++) {
                graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test").setProperty("key", "value");
            }
            assertEquals(count(graph.getVertices()), 50);
            assertEquals(count(graph.getEdges()), 25);
            int counter = 0;
            for (final Edge edge : graph.getEdges("key", "value")) {
                graph.removeEdge(edge);
                counter++;
            }
            assertEquals(counter, 25);
            assertEquals(count(graph.getVertices()), 50);
            assertEquals(count(graph.getEdges()), 0);

        }
        graph.shutdown();
    }
}