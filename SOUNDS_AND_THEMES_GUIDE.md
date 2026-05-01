# 🎮 Hướng Dẫn Sound và Theme cho Tetris

## 📁 Cấu Trúc Thư Mục

```

> Khuyến nghị: đặt đúng 7 file chuẩn ở trên để mỗi event có âm riêng.
> Nếu thiếu file, game vẫn chạy nhờ cơ chế fallback và synth tone.
Sound/
├── move.wav          (Âm thanh di chuyển khối)
├── rotate.wav        (Âm thanh xoay khối)
├── place.wav         (Âm thanh đặt khối xuống đáy)
├── clear.wav         (Âm thanh xóa hàng)
├── tetris.wav        (Âm thanh xóa 4 hàng cùng lúc)
├── levelup.wav       (Âm thanh lên level)
└── gameover.wav      (Âm thanh game over)
```

## 🔊 Hướng Dẫn Tải Sounds

### 🌐 Nguồn Miễn Phí (Free)

**1. Zapsplat** (https://www.zapsplat.com)
   - Tìm các keyword: "game sound", "beep", "click", "score", "level up"
   - Định dạng: WAV, MP3
   - Không cần đăng ký có thể download

**2. Freesound.org** (https://freesound.org)
   - Tìm: "game", "tetris", "beep", "score"
   - Định dạng: OGG, WAV, MP3
   - Chọn Creative Commons license

**3. OpenGameArt.org** (https://opengameart.org)
   - Tìm: "sound effects", "game audio"
   - Tất cả đều có Creative Commons license
   - Chất lượng cao

**4. Mixkit** (https://mixkit.co)
   - Tìm: "game", "notification", "success"
   - Không cần account
   - Miễn phí toàn bộ

**5. Kenney.nl** (https://kenney.nl/assets/category:Audio)
   - Bộ sưu tập hoàn chỉnh cho indie game
   - Chất lượng chuyên nghiệp

### 📋 Danh Sách Cụ Thể Cần Download

| Sound | Tên File | Mô Tả | Nguồn Gợi Ý |
|-------|---------|-------|-------------|
| MOVE | move.wav | Âm bíp nhỏ khi di chuyển | Zapsplat: "beep" |
| ROTATE | rotate.wav | Âm xoay tuân chứng (khác với move) | Freesound: "rotate" hoặc "whoosh" |
| PLACE | place.wav | Âm bầu "tump" khi chạm đáy | Mixkit: "hit" hoặc "drop" |
| CLEAR | clear.wav | Âm "pop" hoặc "success" khi xóa dòng | Kenney: explosion series |
| TETRIS | tetris.wav | Âm "fanfare" hoặc "victory" (4 dòng) | OpenGameArt: "fanfare" |
| LEVEL_UP | levelup.wav | Âm "ding" hoặc "levelup" | Zapsplat: "levelup" hoặc "achievement" |
| GAME_OVER | gameover.wav | Âm "game over" buồn | Freesound: "game over" |

### 🎵 Lưu Ý Kỹ Thuật

- **Định dạng**: Sử dụng **.wav** hoặc **.mp3** (tránh OGG vì Java Swing hỗ trợ hạn chế)
- **Độ dài**: Mỗi sound nên ≤ 1 giây (ngoại trừ game over có thể 2-3 giây)
- **Chất lượng**: 44100 Hz, 16-bit, mono hoặc stereo
- **Dung lượng**: Mỗi file < 500KB

### 🚀 Cách Thêm Sound

1. Download file WAV
2. Đặt vào thư mục `Sound/`
3. Đặt tên theo đúng tên trong danh sách trên
4. Restart game

### 🔁 Fallback và Synth (đã tích hợp trong code)

- Mỗi event âm thanh có nhiều tên file dự phòng.
- Nếu thiếu file chuẩn, game sẽ thử tên thay thế trước khi bỏ qua.
- Nếu không có file nào khả dụng, game phát synth tone tương ứng event để không bị mất âm hoàn toàn.

Ví dụ mapping dự phòng:
- `MOVE`: `move.wav` → `touch floor.wav`
- `ROTATE`: `rotate.wav` → `rotation.wav`
- `CLEAR`: `clear.wav` → `delete line.wav`

### 🧪 Kiểm Tra Trong Settings

Từ menu game:
1. Nhấn `S` để vào **SETTINGS**
2. Xem khung **SFX STATUS**

Ý nghĩa hiển thị:
- `F:<tên file>`: event đang dùng file âm thanh thật
- `SYNTH`: event chưa có file phù hợp, đang dùng âm tổng hợp

**Ví dụ:**
```
Sound/
├── move.wav          ← download từ Zapsplat
├── rotate.wav        ← download từ Freesound
├── place.wav         ← download từ Mixkit
...
```

## 🎨 Theme

Game hiện có **4 themes** có sẵn:

1. **CLASSIC** — Retro Nintendo style
2. **DARK_MODERN** — tetr.io style (mặc định)
3. **NEON** — Cyberpunk aesthetic
4. **RETRO** — 8-bit style

### 🎮 Cách Thay Đổi Theme Trong Game

**Nhấn phím T** để chuyển đến theme tiếp theo. Themes sẽ lặp vòng:
- CLASSIC → DARK_MODERN → NEON → RETRO → CLASSIC → ...

### 💻 Cách Thay Đổi Theme Mặc Định Trong Code

Mở `Main.java` và thêm dòng này sau `frame.pack()`:

```java
// Chọn theme mặc định (trước khi hiển thị)
ThemeManager.setTheme(ThemeManager.Theme.NEON);  // Hoặc CLASSIC, RETRO, DARK_MODERN
```

## 📝 Tín Dụng / Attribution

Khi sử dụng sounds từ các nguồn Creative Commons, hãy ghi danh sách tác giả:

**Ví dụ file ATTRIBUTION.txt:**
```
SOUND ATTRIBUTION:
- move.wav: [Tác giả] từ Zapsplat
- rotate.wav: [Tác giả] từ Freesound.org (CC0)
- place.wav: [Tác giả] từ Mixkit
...
```

---

**Lưu ý:** Tất cả các tài nguyên gợi ý đều **miễn phí** và **hợp pháp** để sử dụng trong indie game (không thương mại). Nếu muốn công bố game, hãy kiểm tra lại license của từng asset.
