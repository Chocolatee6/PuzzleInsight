# 🎮 Puzzle Insight

## 📌 Giới thiệu

Game được phát triển bằng **Java (Swing GUI)** và chạy bằng **Visual Studio Code**.
Người chơi sẽ thực hiện các thao tác như hoán đổi, ghi điểm và vượt qua các màn chơi.

---

## ⚙️ Yêu cầu hệ thống

Trước khi cài đặt, hãy đảm bảo bạn đã cài:

* ☕ **Java Development Kit (JDK) 8 trở lên**
* 🧩 **Visual Studio Code**
* 🔌 Extension Java trong VS Code:

  * Extension Pack for Java (khuyến khích)

---

## 📥 Cài đặt và chạy trên VS Code

### 1. Clone project

```bash
git clone https://github.com/Chocolatee6/PuzzleInsight.git
```

### 2. Mở project bằng VS Code

* Mở VS Code
* Chọn **File → Open Folder**
* Chọn thư mục project vừa clone

---

### 3. Cài extension Java (nếu chưa có)

* Vào tab **Extensions (Ctrl + Shift + X)**
* Tìm: `Extension Pack for Java`
* Cài đặt

---

### 4. Chạy game

#### Cách 1 (dễ nhất)

* Mở file `Main.java`
* Nhấn nút ▶ **Run** ở góc trên

#### Cách 2 (Terminal)

```bash
javac -d bin src/**/*.java
java -cp bin Main
```

---

## 🎮 Cách chơi

* Click chuột để chọn các ô
* Hoán đổi vị trí để tạo chuỗi giống nhau
* Ghi điểm và vượt màn

---

## 📁 Cấu trúc thư mục

```
project-root/
│── src/            # Source code
│── images/         # Âm thanh
│── sounds/         # Hình ảnh
│── bin/            # File biên dịch
│── README.md
```

---

## 🐞 Lỗi thường gặp

**❌ Không thấy nút Run**

* Kiểm tra đã cài Extension Java chưa

**❌ Lỗi không nhận Java**

* Kiểm tra JDK đã cài
* Kiểm tra `JAVA_HOME`

**❌ Lỗi build**

* Đảm bảo file `.java` nằm trong thư mục `src`

---

## 📜 License

Dự án dùng cho mục đích học tập.
