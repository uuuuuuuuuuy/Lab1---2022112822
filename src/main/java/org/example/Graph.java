package org.example; // 指定该类的包名为 org.example

import java.util.*; // 导入 Java 中常用的工具类，如 Map、HashMap、Set、HashSet 等

public class Graph { // 定义一个公共类 Graph，用于表示有向图

    // 定义一个邻接表（adjacency list），用来存储图的结构。
    // 每个键（String）是一个单词（节点），值是一个 Map（邻接节点 -> 权重）
    private final Map<String, Map<String, Integer>> adjList = new HashMap<>();

    /**
     * 向图中添加一条有向边 from -> to。
     * 如果已经存在该边，则把边的“权重”加1（表示该词序对出现次数）。
     */
    public void addEdge(String from, String to) {
        from = from.toLowerCase(); // 把起点单词转换成小写，避免大小写混淆（统一处理）
        to = to.toLowerCase();     // 同上，对终点单词也统一小写

        // 如果起点还不在图中，就创建一个新的邻接表项
        adjList.putIfAbsent(from, new HashMap<>());

        // 获取起点的邻接表（即 from 节点所有的出边）
        Map<String, Integer> neighbors = adjList.get(from);

        // 将 to 节点加入 from 的邻接表中，如果已有则权重+1；否则初始为1
        neighbors.put(to, neighbors.getOrDefault(to, 0) + 1);
    }

    /**
     * 获取整个图的邻接表结构。
     * 返回类型是 Map<String, Map<String, Integer>>
     */
    public Map<String, Map<String, Integer>> getAdjList() {
        return adjList;
    }

    /**
     * 获取图中的所有节点（不只是起点，还包括终点）
     * 因为终点节点可能不会作为起点出现在 adjList 的 key 中
     */
    public Set<String> getNodes() {
        // 创建一个集合，先把所有起点节点加进去
        Set<String> nodes = new HashSet<>(adjList.keySet());

        // 遍历所有邻接表，把所有终点节点也加进去
        for (Map<String, Integer> neighbors : adjList.values()) {
            nodes.addAll(neighbors.keySet());
        }

        return nodes; // 返回包含所有节点的集合
    }

    /**
     * 获取某个节点的所有邻居（它能连接到的节点）
     * 如果节点不存在，返回一个空的邻接表（即空 map）
     */
    public Map<String, Integer> getNeighbors(String node) {
        return adjList.getOrDefault(node.toLowerCase(), new HashMap<>());
    }

    /**
     * 获取两个节点之间的边的权重
     * 如果边不存在，则返回0
     */
    public int getEdgeWeight(String from, String to) {
        return adjList.getOrDefault(from.toLowerCase(), new HashMap<>())
                .getOrDefault(to.toLowerCase(), 0);
    }

    /**
     * 判断图中是否包含指定的节点（只检查是否作为起点存在）
     */
    /**
     * 判断图中是否包含指定的节点（包括作为起点或终点）
     */
    public boolean containsNode(String word) {
        word = word.toLowerCase();

        // 起点中是否包含
        if (adjList.containsKey(word)) {
            return true;
        }

        // 在终点中是否出现过
        for (Map<String, Integer> neighbors : adjList.values()) {
            if (neighbors.containsKey(word)) {
                return true;
            }
        }

        return false;
    }
}
