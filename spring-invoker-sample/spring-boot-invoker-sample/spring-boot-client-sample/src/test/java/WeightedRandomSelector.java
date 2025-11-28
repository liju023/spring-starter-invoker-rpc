import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandomSelector<T> {
    private final List<T> items = new ArrayList<>();
    private final List<Integer> cumulativeWeights = new ArrayList<>();
    private int totalWeight = 0;

    // 添加带权重的选项
    public void addItem(T item, int weight) {
        if (weight <= 0) throw new IllegalArgumentException("Weight must be positive");
        items.add(item);
        totalWeight += weight;
        cumulativeWeights.add(totalWeight);
    }

    // 根据权重随机选择一个选项
    public T select() {
        if (items.isEmpty()) throw new IllegalStateException("No items available");
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        // 二分查找优化查找速度
        int index = Collections.binarySearch(cumulativeWeights, randomWeight);
        if (index < 0) {
            index = -index - 1; // 转换未找到的插入点
        }
        return items.get(index);
    }

    // 示例用法
    public static void main(String[] args) {
        WeightedRandomSelector<String> selector = new WeightedRandomSelector<>();
        selector.addItem("A", 3);  // 权重3
        selector.addItem("B", 2);  // 权重2
        selector.addItem("C", 5);  // 权重5

        // 测试分布
        Map<String, Integer> count = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            String selected = selector.select();
            count.put(selected, count.getOrDefault(selected, 0) + 1);
        }
        System.out.println(count); 
        // 预期近似比例 3:2:5 → 例如 {A=3000, B=2000, C=5000}
    }
}