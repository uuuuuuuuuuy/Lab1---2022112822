package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextGraphAppTest {

    @Before
    public void setUp() {
        TextGraphApp.resetGraph();  // ✅ 清空旧图
        TextGraphApp.graph.addEdge("a", "x");
        TextGraphApp.graph.addEdge("x", "b");
    }

    @Test
    public void testValidBridge() {
        String result = TextGraphApp.queryBridgeWords("a", "b");
        assertTrue(result.contains("x"));
    }

    @Test
    public void testWord2NotExist() {
        String result = TextGraphApp.queryBridgeWords("a", "z");
        assertEquals("No \"z\" in the graph!", result);
    }

    @Test
    public void testWord1NotExist() {
        String result = TextGraphApp.queryBridgeWords("z", "b");
        assertEquals("No \"z\" in the graph!", result);
    }

    @Test
    public void testEmptyWord2() {
        String result = TextGraphApp.queryBridgeWords("a", "");
        assertTrue(result.contains("Shortest path"));
    }

    @Test
    public void testNoBridge() {
        TextGraphApp.resetGraph(); // 清空旧图

        // 构建一个没有桥接词的图：a -> m，n -> b（a 和 b 之间无连接）
        TextGraphApp.graph.addEdge("a", "m");
        TextGraphApp.graph.addEdge("n", "b");

        String result = TextGraphApp.queryBridgeWords("a", "b");
        //System.out.println("【测试输出】" + result);  // ✅ 查看实际输出

        assertTrue("预期输出中包含 'No bridge words'，但实际为：" + result,
                result.contains("No bridge words"));
    }

}
