# Guest Guess ITB

### Latar Belakang

Jika anda adalah mahasiswa **Institut Teknologi Bandung** alias **ITB** kemudian anda ingin mencari tempat-tempat dan *landmark* yang menarik dan bisa anda kunjungi, atau anda ingin mencari tempat yang memiliki *Vending Machine* untuk membeli minuman ketika anda haus, maka bawalah ~~pacar~~ **Guest Guess ITB** bersama anda!

**Guest Guess ITB** adalah sebuah aplikasi hasil pengembangan untuk tugas mata kuliah **IF3111 Pemrograman Aplikasi Berbasis Platform**. Aplikasi ini berbasis sistem operasi Android, dan mudah digunakan! **Wew!**

### Fitur apa saja yang ada di aplikasi ini?

* Aplikasi akan mengirimkan request untuk tempat baru di ITB melalui **Socket**
* Anda dapat melihat lokasi tempat yang telah diterima oleh aplikasi melalui **Google Maps** yang tersedia di dalam aplikasi
* Tersedia gambar panah yang akan menunjukkan arah Utara
* Anda dapat mengambil foto lokasi yang telah dikunjungi
* Anda dapat meminta lokasi baru dengan menebak nama tempat yang baru saja anda kunjungi

### Bagaimana Cara Menggunakan Aplikasi Ini?

1. Buka aplikasi dan masukkan NIM anda di *field* yang tersedia. Kemudian, klik tombol *REQUEST CHALLENGE!* Pada saat itu, aplikasi akan mengirimkan *request* ke *server* untuk lokasi baru.
2. Tunggu sesaat hingga *server* membalas *request* anda, yang berisi koordinat untuk lokasi baru tersebut. Koordinat akan ditampilkan dalam bentuk *Marker* pada *Google Maps* yang muncul.
3. Silahkan pergi ke tempat tersebut!
4. Jika sudah sampai di tempat, Buka kamera melalui aplikasi, kemudian foto lokasi tersebut.
5. Jawab nama tempat tersebut melalui tombol Submit Answer yang ada di pojok kanan bawah layar, dengan memilih nama tempat yang tersedia di *dropdown*.
6. Jika anda yakin, tekan tombol *Submit Answer*. Jawaban akan dikirimkan ke *server*, beserta dengan koordinat lokasi anda sekarang.
7. Jika anda mendapatkan pesan berhasil, maka anda akan diberikan koordinat untuk tempat selanjutnya!
8. Ulangi hingga 3 kali, dan anda telah selesai menggunakan aplikasi.

### Screenshot Layar

![Panduan 1](http://i.imgur.com/SkA4587.png)

![Panduan 2](http://i.imgur.com/9hv5R43.png)

![Panduan 3](http://i.imgur.com/i15K1yU.png)

![Panduan 4](http://i.imgur.com/EcW1Zf4.png)

![Panduan 5](http://i.imgur.com/UyVjZ3Y.png)

### Spesifikasi Detil Server
Komunikasi ke server dengan alamat: **167.205.24.132** pada port **3111**

### Prerequisites
Agar dapat menjalankan aplikasi ini, Smartphone Android anda harus memenuhi hal berikut:
* Memiliki Kamera
* Sudah Menginstall Google Play Services
* Memiliki sensor **Accelerometer** dan **Magnetic Field Sensor** untuk Kompas
* Mengaktifkan fitur **Location**

### Lokasi Deliverables
[Source Files](http://gitlab.informatika.org/ahmadnaufal/Tubes1-Android/tree/master/app/src/main)
[Binary (APK)](http://gitlab.informatika.org/ahmadnaufal/Tubes1-Android/blob/master/app-debug.apk)