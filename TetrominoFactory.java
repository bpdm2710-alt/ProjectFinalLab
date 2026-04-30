import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TetrominoFactory {

    // ============================================================
    // 7-BAG RANDOMIZER
    // Dùng LinkedList để vừa rút khối ở đầu (poll), vừa đọc trước (get)
    // ============================================================
    private LinkedList<Integer> queue;

    public TetrominoFactory() {
        queue = new LinkedList<>();
        refill(); // Nạp túi thứ 1
        refill(); // Nạp túi thứ 2 để đảm bảo luôn đủ khối cho preview
    }

    public Tetromino next() {
        // Nếu hàng đợi sắp cạn (còn lại 1 túi), nạp thêm túi mới vào đuôi
        if (queue.size() <= 7) {
            refill();
        }
        return new Tetromino(queue.poll());
    }

    private void refill() {
        List<Integer> bag = new ArrayList<>();
        for (int i = 0; i < 7; i++) bag.add(i);
        Collections.shuffle(bag);
        queue.addAll(bag);
    }

    // ============================================================
    // PREVIEW — GameController gọi để SidebarPanel hiển thị
    // Luôn chính xác tuyệt đối vì chỉ đọc từ hàng đợi thực tế
    // ============================================================
    public List<Tetromino> preview(int count) {
        List<Tetromino> result = new ArrayList<>();
        // Chỉ việc đọc, không thay đổi state của queue
        for (int i = 0; i < count && i < queue.size(); i++) {
            result.add(new Tetromino(queue.get(i)));
        }
        return result;
    }

    public void reset() {
        queue.clear();
        refill();
        refill();
    }
}