# 🎮 Tetris Game — Hướng Dẫn Chơi

## 🎯 Tính Năng Chính

- ✅ Điều khiển Tetris chuẩn (tetr.io-style)
- ✅ DAS/ARR tối ưu cho gameplay mượt
- ✅ Wall Kick SRS cho xoay chuyên nghiệp
- ✅ Lock Delay với Infinity Rule (tối đa 15 resets)
- ✅ Hold Piece và Preview 3 khối tiếp theo
- ✅ 4 Themes (Classic, Dark Modern, Neon, Retro)
- ✅ Responsive UI (phóng/thu theo cửa sổ)
- ✅ Fullscreen Support (F11 / Esc)

---

## ⌨️ Bàn Phím Điều Khiển

### Gameplay

| Phím | Tác Vụ |
|------|--------|
| **Mũi Tên Trái** | Di chuyển trái |
| **Mũi Tên Phải** | Di chuyển phải |
| **Mũi Tên Xuống** | Soft drop (rơi nhanh) |
| **X** hoặc **Mũi Tên Lên** | Xoay clockwise |
| **Z** | Xoay counter-clockwise |
| **A** | Xoay 180° |
| **Space** | Hard drop (rơi tới đáy ngay) |
| **C** | Hold piece (giữ khối) |
| **P** | Tạm dừng / Tiếp tục |
| **S** | Mở Settings từ Menu |
| **T** | Chuyển theme (CLASSIC → NEON → ...) |
| **M** | Bật/Tắt nhạc nền (BGM) |
| **N** | Bật/Tắt hiệu ứng âm thanh (SFX) |
| **F8 / F7** | Tăng/Giảm âm lượng BGM |
| **F6 / F5** | Tăng/Giảm âm lượng SFX |

### Toàn Cục

| Phím | Tác Vụ |
|------|--------|
| **F11** | Fullscreen toggle |
| **Esc** | Thoát fullscreen |
| **R** | Restart khi Game Over |

---

## 🎨 Themes

Nhấn **T** để chuyển theme:

### 1. CLASSIC
- Retro Nintendo style
- Nền đen cổ điển
- Lưới xám

### 2. DARK_MODERN (Mặc Định)
- tetr.io-inspired
- Nền đen hiện đại
- Chữ trắng sáng

### 3. NEON
- Cyberpunk aesthetic
- Nền tím-xanh đen
- Chữ cyan neon / magenta neon
- Lưới cyan sáng

### 4. RETRO
- 8-bit style
- Nền xanh-navy
- Chữ xanh nhạt
- Lưới xanh sẫm

---

## 🔊 Âm Thanh

Game hỗ trợ 7 loại âm thanh:
- **Move** — Di chuyển khối
- **Rotate** — Xoay khối
- **Place** — Đặt khối xuống đáy
- **Clear** — Xóa 1-3 dòng
- **Tetris** — Xóa 4 dòng cùng lúc
- **Level Up** — Lên level mới
- **Game Over** — Kết thúc trò chơi

Lưu ý về hệ thống âm thanh hiện tại:
- Game sẽ ưu tiên đúng tên file chuẩn cho từng event: `move.wav`, `rotate.wav`, `place.wav`, `clear.wav`, `tetris.wav`, `levelup.wav`, `gameover.wav`.
- Nếu thiếu file chuẩn, game tự fallback sang các file gần nhất đang có trong thư mục `Sound/`.
- Nếu vẫn không tìm thấy file phù hợp, game phát âm tổng hợp (synth tone) để không bị im lặng.
- Trong **Settings**, mục **SFX STATUS** hiển thị event nào đang dùng file thật và event nào đang dùng synth.

**Xem hướng dẫn:** Mở file [SOUNDS_AND_THEMES_GUIDE.md](SOUNDS_AND_THEMES_GUIDE.md)

---

## 📊 Điểm & Level

- **Điểm:**
  - 1 dòng xóa = 100 × Level
  - 2 dòng xóa = 300 × Level
  - 3 dòng xóa = 500 × Level
  - 4 dòng xóa (Tetris) = 800 × Level

- **Level:**
  - Tăng mỗi 10 dòng xóa
  - Level càng cao, khối rơi càng nhanh
  - Tốc độ rơi = 800 - (Level - 1) × 70 ms

---

## 🕹️ Tips Chơi

1. **Soft Drop** (Mũi Tên Xuống) giúp điều chỉnh khối nhanh hơn
2. **Hard Drop** (Space) tấn công trực tiếp nhưng mất chance di chuyển
3. **Lock Delay** cho 500ms để di chuyển/xoay khối trước khi khóa (tối đa 15 lần)
4. **Hold Piece** hữu ích để tránh "I-block drought"
5. **Preview 3 khối** tiếp theo để lên chiến lược
6. **Ghost Piece** (bóng mờ) hiển thị vị trí hạ cánh
7. Vào **Settings** từ menu để kiểm tra nhanh trạng thái âm thanh (`SFX STATUS`)

---

## 📁 Cấu Trúc Project

```
ProjectFinalLab-1/
├── Main.java                    — Entry point
├── GamePanel.java               — Vẽ bảng chơi
├── SidebarPanel.java            — Hiển thị score, hold, preview
├── GameController.java          — Xử lý input & logic
├── Board.java                   — Collision detection, line clear
├── Tetromino.java               — Khối Tetris & xoay SRS
├── TetrominoFactory.java        — 7-bag randomizer
├── GameState.java               — Quản lý trạng thái game
├── ThemeManager.java            — Quản lý themes
├── SoundManager.java            — Phát âm thanh
├── Sound/                       — Thư mục âm thanh
│   ├── move.wav
│   ├── rotate.wav
│   ├── place.wav
│   ├── clear.wav
│   ├── tetris.wav
│   ├── levelup.wav
│   └── gameover.wav
├── SOUNDS_AND_THEMES_GUIDE.md   — Hướng dẫn thêm sound & theme
└── README.md                    — File này
```

---

## 🔧 Chỉnh Sửa

### Thay Đổi Theme Mặc Định

Mở `Main.java` → thêm sau `frame.pack()`:
```java
ThemeManager.setTheme(ThemeManager.Theme.NEON);
```

### Thay Đổi Tốc Độ DAS (Delayed Auto Shift)

Mở `GameController.java` → sửa:
```java
private final int DAS = 130;  // Tăng để chậm hơn, giảm để nhanh hơn
```

### Thay Đổi Độ Dốc Lock Delay

Mở `GameController.java` → sửa:
```java
private final int LOCK_DELAY = 500;  // Tính bằng milliseconds
```

---

## 🎓 Kiến Trúc Code

- **Separation of Concerns** — Logic game, UI, Sound riêng biệt
- **Observer Pattern** — GameState thông báo cho UI khi thay đổi
- **Singleton** — SoundManager & ThemeManager
- **MVC-like** — Controller xử lý input, Panel vẽ, State quản lý
- **Thread-safe** — Input loop riêng, render loop dùng Swing Timer

---

## 📝 License & Attribution

**Nguồn Code:**
- Tetromino shapes: Chuẩn Tetris Guideline
- Wall Kick: SRS (Tetris Guideline)
- UI: Java Swing
- Themes: Tự tạo (inspired by tetr.io, NES Tetris)

**Âm Thanh:** Xem [SOUNDS_AND_THEMES_GUIDE.md](SOUNDS_AND_THEMES_GUIDE.md)

---

Chúc bạn chơi vui! 🎮✨
