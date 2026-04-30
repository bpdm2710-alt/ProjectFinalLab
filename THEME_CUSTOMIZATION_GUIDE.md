# 🎨 Hướng Dẫn Tạo Theme Mới

Bạn có thể dễ dàng tạo thêm themes mới bằng cách chỉnh sửa file `ThemeManager.java`.

## 📋 Bước 1: Thêm Theme Mới vào Enum

Mở `ThemeManager.java` và thêm theme vào enum `Theme`:

```java
public enum Theme {
    CLASSIC,
    DARK_MODERN,
    NEON,
    RETRO,
    MY_CUSTOM_THEME  // ← Thêm dòng này
}
```

## 🎨 Bước 2: Tạo Hàm Palette cho Theme Mới

Thêm hàm mới dưới các hàm theme khác:

```java
private static ThemePalette getMyCustomThemePalette() {
    ThemePalette p = new ThemePalette();
    p.background = new Color(50, 50, 50);           // Nền chính
    p.grid = new Color(100, 100, 100);              // Lưới bảng
    p.text = new Color(255, 255, 255);              // Chữ chính
    p.textSecondary = new Color(200, 200, 200);     // Chữ phụ
    p.boardBackground = new Color(30, 30, 30);      // Nền bảng chơi
    p.sidebarBackground = new Color(40, 40, 40);    // Nền sidebar
    return p;
}
```

### Gợi Ý Màu Sắc

- **background** — Màu chính của ứng dụng (ngoài bảng)
- **grid** — Màu lưới trên bảng (nền ô)
- **text** — Màu chữ chính (score, level)
- **textSecondary** — Màu chữ phụ (label)
- **boardBackground** — Màu nền bảng chơi
- **sidebarBackground** — Màu nền sidebar (Hold, Next)

### 🌈 Ví Dụ Các Bảng Màu

#### Pastel Dream
```java
p.background = new Color(230, 220, 240);        // Tím nhạt
p.grid = new Color(200, 180, 220);              // Lưới tím nhạt
p.text = new Color(60, 40, 100);                // Chữ tím đậm
p.textSecondary = new Color(120, 100, 150);     // Chữ tím nhạt
p.boardBackground = new Color(240, 235, 250);   // Nền tím rất nhạt
p.sidebarBackground = new Color(220, 210, 235); // Nền sidebar tím nhạt
```

#### Ocean Blue
```java
p.background = new Color(10, 40, 80);           // Xanh đậm
p.grid = new Color(30, 80, 150);                // Xanh biển
p.text = new Color(100, 200, 255);              // Xanh sáng
p.textSecondary = new Color(70, 150, 200);      // Xanh biển nhạt
p.boardBackground = new Color(5, 20, 50);       // Xanh rất đậm
p.sidebarBackground = new Color(10, 35, 70);    // Xanh sidebar đậm
```

#### Sunset Orange
```java
p.background = new Color(255, 100, 0);          // Cam sáng
p.grid = new Color(200, 80, 0);                 // Cam đậm
p.text = new Color(255, 255, 200);              // Chữ vàng
p.textSecondary = new Color(200, 150, 100);     // Chữ be
p.boardBackground = new Color(100, 50, 0);      // Nền nâu đậm
p.sidebarBackground = new Color(150, 70, 0);    // Nền cam đậm
```

#### Matrix Green
```java
p.background = new Color(0, 20, 0);             // Xanh rất đậm
p.grid = new Color(0, 100, 0);                  // Xanh matrix
p.text = new Color(0, 255, 0);                  // Xanh sáng
p.textSecondary = new Color(0, 150, 0);         // Xanh nhạt
p.boardBackground = new Color(0, 10, 0);        // Xanh rất rất đậm
p.sidebarBackground = new Color(0, 15, 0);      // Xanh sidebar đen
```

## 🔄 Bước 3: Thêm vào Switch Statement

Tìm hàm `getPalette()` và thêm case mới:

```java
public static ThemePalette getPalette() {
    return switch (currentTheme) {
        case CLASSIC -> getClassicPalette();
        case DARK_MODERN -> getDarkModernPalette();
        case NEON -> getNeonPalette();
        case RETRO -> getRetroPalette();
        case MY_CUSTOM_THEME -> getMyCustomThemePalette();  // ← Thêm dòng này
    };
}
```

## 🧪 Bước 4: Test Theme

1. Compile: `javac Main.java`
2. Chạy game: `java Main`
3. Nhấn **T** để chuyển đến theme mới

---

## 🎯 Checklist Tạo Theme Mới

- [ ] Thêm tên theme vào enum `Theme`
- [ ] Tạo hàm `get[ThemeName]Palette()`
- [ ] Thêm case vào `getPalette()` switch
- [ ] Compile test
- [ ] Nhấn **T** để xem theme trong game

---

## 💡 Tips

### Dark Theme Tốt cho Mắt
```java
p.background = new Color(20, 20, 20);
p.text = new Color(220, 220, 220);
```

### Vibrant Theme (Rực Rỡ)
```java
p.grid = new Color(255, 100, 150);      // Hồng neon
p.text = new Color(255, 255, 0);        // Vàng sáng
```

### High Contrast (Dễ Nhìn)
```java
p.background = new Color(255, 255, 255);  // Trắng
p.text = new Color(0, 0, 0);              // Đen
p.grid = new Color(128, 128, 128);        // Xám
```

---

Xong rồi! Theme mới sẽ tự động xuất hiện trong vòng xoay khi bấm **T**. 🎨✨
