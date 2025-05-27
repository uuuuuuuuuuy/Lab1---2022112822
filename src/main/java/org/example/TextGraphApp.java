package org.example;

import java.io.*;
import java.util.*;

public class TextGraphApp {
    static final Graph graph = new Graph();

    private static List<String> corpusWords = new ArrayList<>();
    private static final double DAMPING_FACTOR = 0.85;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入文本文件路径：");
        String filepath = scanner.nextLine();
        loadTextAndBuildGraph(filepath);

        while (true) {
            System.out.println("\n选择功能：");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算最短路径");
            System.out.println("5. 计算PageRank");
            System.out.println("6. 随机游走");
            System.out.println("0. 退出");

            int option = scanner.nextInt();
            scanner.nextLine(); // 处理回车

            switch (option) {
                case 1 -> showDirectedGraph();
                case 2 -> {
                    handleBridgeWordsQuery();
                }
                case 3 -> {
                    System.out.println("输入一句文本：");
                    String line = scanner.nextLine();
                    System.out.println(generateNewText(line));
                }
                case 4 -> {
                    System.out.println("请输入一个或两个单词（用空格分隔）：");
                    String line = scanner.nextLine().trim();
                    String[] words = line.split("\\s+");

                    if (words.length == 1) {
                        String word = words[0];
                        System.out.println("计算从 " + word + " 到其他所有节点的最短路径：");
                        System.out.println(findShortestPath(word)); // ✅
                    } else if (words.length == 2) {
                        String word1 = words[0];
                        String word2 = words[1];
                        System.out.println("计算从 " + word1 + " 到 " + word2 + " 的最短路径：");
                        System.out.println(calcShortestPath(word1, word2)); // ✅

                    } else {
                        System.out.println("输入格式错误，请输入一个或两个单词。");
                    }
                }

                case 5 -> {
                    System.out.println("输入单词：");
                    String word = scanner.next();
                    System.out.printf("PageRank(%s) = %.5f\n", word, calPageRankWithTF(word, corpusWords));
                }
                case 6 -> System.out.println(randomWalk());
                case 0 -> {
                    System.out.println("退出程序。");
                    return;
                }
                default -> System.out.println("无效选项！");
            }
        }
    }

    private static void loadTextAndBuildGraph(String filepath) {
        try {
            corpusWords.clear(); // 清空上一次的记录
            Scanner fileScanner = new Scanner(new File(filepath));
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().toLowerCase().replaceAll("[^a-z\\s]", "");
                String[] words = line.trim().split("\\s+");

                for (int i = 0; i < words.length - 1; i++) {
                    graph.addEdge(words[i], words[i + 1]);
                    corpusWords.add(words[i]);
                }

                if (words.length > 0) {
                    corpusWords.add(words[words.length - 1]);
                }
            }
            System.out.println("图构建成功，共有节点：" + graph.getNodes().size());
        } catch (FileNotFoundException e) {
            System.out.println("找不到文件: " + filepath);
        }
    }

    // 展示图（命令行格式）
    //showDirectedGraph() 方法遍历图的所有节点和它们的邻接节点，输出图的所有边以及它们的权重（这里假设每条边的权重为 1）。
    //同时，调用 exportGraphToDot 方法将图导出为 .dot 文件，然后调用 generateGraphImage 生成图像。
    public static void showDirectedGraph() {
        System.out.println("当前有向图：");
        for (String from : graph.getNodes()) {
            Map<String, Integer> edges = graph.getNeighbors(from);
            for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                System.out.printf("%s -> %s [weight=%d]\n", from, entry.getKey(), entry.getValue());
            }
        }

        // 导出并生成图像
        String dotPath = "graph_output.dot";
        String imgPath = "graph_output.png";
        exportGraphToDot(dotPath);
        generateGraphImage(dotPath, imgPath);
    }

    // 处理桥接词查询的函数
    public static void handleBridgeWordsQuery() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入两个单词（用空格分隔）：");
        String inputLine = scanner.nextLine().trim();

        String[] words = inputLine.split("\\s+");  // 按空格分隔，支持多个空格

        // 如果没有有效输入，提示重新输入
        if (words.length == 0 || words[0].isEmpty()) {
            System.out.println("未检测到有效输入，请重新输入。");
            return;
        }

        // 处理只输入一个单词的情况
        if (words.length == 1) {
            System.out.println(queryBridgeWords(words[0], ""));
        }
        // 处理输入两个单词的情况
        else if (words.length >= 2) {
            System.out.println(queryBridgeWords(words[0], words[1]));
        } else {
            System.out.println("未检测到有效输入，请重新输入。");
        }
    }

    // queryBridgeWords函数
    public static String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!nodeExists(word1)) {
            return "No \"" + word1 + "\" in the graph!";
        }

        if (word2.isEmpty()) {
            return findShortestPath(word1);
        }

        if (!nodeExists(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        }

        Set<String> bridges = new HashSet<>();
        for (String candidate : graph.getNeighbors(word1).keySet()) {
            if (graph.getNeighbors(candidate).containsKey(word2)) {
                bridges.add(candidate);
            }
        }

        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " +
                    String.join(", ", bridges) + ".";
        }
    }


    // 辅助函数检查节点是否存在于图中
    public static boolean nodeExists(String word) {
        return graph.containsNode(word) || graph.getNeighbors(word).size() > 0;
    }



    // 计算最短路径的方法
    public static String findShortestPath(String startWord) {
        startWord = startWord.toLowerCase();

        if (!graph.containsNode(startWord)) {
            return "No \"" + startWord + "\" in the graph!";
        }

        // BFS初始化
        Queue<String> queue = new LinkedList<>();
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // 初始化起始节点
        queue.add(startWord);
        visited.add(startWord);
        distances.put(startWord, 0);

        // 广度优先搜索
        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            int currentDistance = distances.get(currentNode);

            for (String neighbor : graph.getNeighbors(currentNode).keySet()) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);
                    predecessors.put(neighbor, currentNode);
                }
            }
        }

        // 生成结果
        StringBuilder result = new StringBuilder();
        for (String node : graph.getNodes()) {
            if (!node.equals(startWord)) {
                if (distances.containsKey(node)) {
                    result.append("Shortest path from \"" + startWord + "\" to \"" + node + "\": ");
                    List<String> path = new ArrayList<>();
                    String current = node;
                    while (current != null) {
                        path.add(current);
                        current = predecessors.get(current);
                    }
                    Collections.reverse(path);
                    result.append(String.join(" -> ", path));
                    result.append(" (Distance: " + distances.get(node) + ")\n");
                } else {
                    result.append("No path from \"" + startWord + "\" to \"" + node + "\".\n");
                }
            }
        }

        return result.toString();
    }

    // 插入桥接词生成新文本
    public static String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().replaceAll("[^a-zA-Z ]", " ").split("\\s+");
        StringBuilder result = new StringBuilder(words[0]);

        Random rand = new Random();
        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i], w2 = words[i + 1];
            Set<String> bridges = new HashSet<>();
            for (String b : graph.getNeighbors(w1).keySet()) {
                if (graph.getNeighbors(b).containsKey(w2)) {
                    bridges.add(b);
                }
            }
            if (!bridges.isEmpty()) {
                List<String> list = new ArrayList<>(bridges);
                String bridge = list.get(rand.nextInt(list.size()));
                result.append(" ").append(bridge);
            }
            result.append(" ").append(w2);
        }
        return result.toString();
    }

    // 最短路径（Dijkstra）
    // 最短路径（Dijkstra + 回溯所有路径）
    public static String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "图中不存在输入的单词！";
        }

        // Dijkstra 最短距离
        Map<String, Integer> dist = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>();
        for (String node : graph.getNodes()) {
            dist.put(node, Integer.MAX_VALUE);
            predecessors.put(node, new ArrayList<>());
        }

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        dist.put(word1, 0);
        pq.add(word1);

        while (!pq.isEmpty()) {
            String curr = pq.poll();
            int currDist = dist.get(curr);

            for (Map.Entry<String, Integer> entry : graph.getNeighbors(curr).entrySet()) {
                String neighbor = entry.getKey();
                int weight = entry.getValue();
                int newDist = currDist + weight;

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    predecessors.get(neighbor).clear();
                    predecessors.get(neighbor).add(curr);
                    pq.add(neighbor);
                } else if (newDist == dist.get(neighbor)) {
                    predecessors.get(neighbor).add(curr);
                }
            }
        }

        if (dist.get(word2) == Integer.MAX_VALUE) {
            return "无法从 " + word1 + " 到达 " + word2;
        }

        // 回溯所有路径
        List<List<String>> allPaths = new ArrayList<>();
        LinkedList<String> currentPath = new LinkedList<>();
        currentPath.add(word2);
        backtrackPaths(word2, word1, predecessors, currentPath, allPaths);

        StringBuilder result = new StringBuilder();
        result.append("共找到 ").append(allPaths.size()).append(" 条最短路径，长度为 ").append(dist.get(word2)).append("：\n");
        int count = 1;
        for (List<String> path : allPaths) {
            Collections.reverse(path);
            result.append("路径 ").append(count++).append(": ");
            result.append(String.join(" -> ", path)).append("\n");
        }

        return result.toString();
    }

    // 回溯所有路径的辅助方法
    private static void backtrackPaths(String current, String start, Map<String, List<String>> predecessors,
                                       LinkedList<String> path, List<List<String>> result) {
        if (current.equals(start)) {
            result.add(new ArrayList<>(path));
            return;
        }

        for (String pred : predecessors.get(current)) {
            path.addLast(pred);
            backtrackPaths(pred, start, predecessors, path, result);
            path.removeLast();
        }
    }


    public static double calPageRankWithTF(String word, List<String> corpusWords) {
        int N = graph.getNodes().size();  // 节点总数
        Map<String, Double> pr = new HashMap<>();
        Map<String, Integer> tf = new HashMap<>();
        double totalCount = 0.0;

        // 1. 统计词频
        for (String w : corpusWords) {
            w = w.toLowerCase();
            if (graph.containsNode(w)) {
                tf.put(w, tf.getOrDefault(w, 0) + 1);
                totalCount += 1;
            }
        }

        // 2. 用词频归一化作为初始 PageRank 值（若某节点不在词频中，则均分剩余）
        double tfSum = tf.values().stream().mapToInt(i -> i).sum();
        double defaultPr = (1.0 - tfSum / totalCount) / (N - tf.size());  // 给未出现的节点均分剩余
        for (String node : graph.getNodes()) {
            if (tf.containsKey(node)) {
                pr.put(node, tf.get(node) / totalCount);  // 权重初始化
            } else {
                pr.put(node, defaultPr);  // 均匀初始化
            }
        }

        // 3. PageRank 主体迭代部分
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            double danglingSum = 0.0;

            for (String node : graph.getNodes()) {
                if (graph.getNeighbors(node).isEmpty()) {
                    danglingSum += pr.get(node);
                }
            }

            for (String node : graph.getNodes()) {
                double sum = 0.0;

                for (String other : graph.getNodes()) {
                    if (graph.getNeighbors(other).containsKey(node)) {
                        int totalOut = graph.getNeighbors(other).values().stream().mapToInt(i -> i).sum();
                        sum += pr.get(other) / totalOut;
                    }
                }

                newPr.put(node, (1 - DAMPING_FACTOR) / N + DAMPING_FACTOR * sum + DAMPING_FACTOR * (danglingSum / N));
            }

            pr = newPr;
        }

        return pr.getOrDefault(word.toLowerCase(), 0.0);
    }

    // 随机游走
    public static String randomWalk() {
        List<String> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.isEmpty()) return "图为空！";

        Random rand = new Random();
        String curr = nodes.get(rand.nextInt(nodes.size()));
        Set<String> visitedEdges = new HashSet<>();
        StringBuilder result = new StringBuilder(curr);

        Scanner scanner = new Scanner(System.in);
        System.out.println("开始随机游走（每秒走一步），按 Enter 停止：");

        while (true) {
            Map<String, Integer> neighbors = graph.getNeighbors(curr);
            if (neighbors.isEmpty()) break;

            List<String> choices = new ArrayList<>(neighbors.keySet());
            String next = choices.get(rand.nextInt(choices.size()));
            String edge = curr + "->" + next;

            if (visitedEdges.contains(edge)) break;

            visitedEdges.add(edge);
            curr = next;
            result.append(" ").append(curr);
            System.out.println("当前位置: " + curr);

            // 等待 1 秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("中断错误：" + e.getMessage());
            }

            // 检查用户是否按下回车终止
            try {
                if (System.in.available() > 0) {
                    scanner.nextLine(); // 消费输入
                    System.out.println("用户终止了随机游走。");
                    break;
                }
            } catch (IOException e) {
                System.err.println("读取输入失败：" + e.getMessage());
            }
        }

        // 写入文件
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"));
            writer.write(result.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("写入文件失败：" + e.getMessage());
        }

        return "随机游走结果：\n" + result.toString();
    }


    public static void exportGraphToDot(String dotFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write("digraph G {\n");
            for (String from : graph.getNodes()) {
                Map<String, Integer> edges = graph.getNeighbors(from);
                for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                    String to = entry.getKey();
                    int weight = entry.getValue();
                    writer.write(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight));
                }
            }
            writer.write("}\n");
            System.out.println("已导出为 DOT 文件：" + dotFilePath);
        } catch (IOException e) {
            System.err.println("导出 DOT 文件失败：" + e.getMessage());
        }
    }
    public static void generateGraphImage(String dotFilePath, String outputPath) {
        try {
            // 调用系统命令执行 dot -> png 转换
            Process process = new ProcessBuilder("dot", "-Tpng", dotFilePath, "-o", outputPath).start();
            process.waitFor();
            System.out.println("图像文件已生成：" + outputPath);
        } catch (IOException | InterruptedException e) {
            System.err.println("生成图像失败：" + e.getMessage());
        }
    }
    // 添加到 TextGraphApp.java 的类中
    public static void resetGraph() {
        graph.getAdjList().clear();
        corpusWords.clear();
    }

}
