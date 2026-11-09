# Android App con YOLO26 🚀

**Android App con YOLO26** es una aplicación móvil de visión por computadora para Android que realiza **detección de objetos en tiempo real** utilizando el flujo de cámara en vivo y modelos de inteligencia artificial de última generación como **Ultralytics YOLOv26n**, **YOLOv11** y **TensorFlow Lite**.

---

## 👨‍💻 Creador y Desarrollador
* **Autor:** M.C. Emmanuelle Jasso
* **Proyecto:** Visión por Computadora & Detección de Objetos en Android

---

## ✨ Características Principales

* ⚡ **Detección en Tiempo Real:** Procesamiento fluido de fotogramas de cámara usando **CameraX** y **TensorFlow Lite**.
* 🧠 **Soporte Multimodelo:**
  * **Ultralytics YOLOv26n** (Alta precisión).
  * **Ultralytics YOLOv11n**.
  * **MobileNet SSD V1** (Máxima velocidad).
  * **EfficientDet Lite0, Lite1 y Lite2**.
* 🏷️ **Filtrado por Campos Semánticos:** Filtra objetos detectados en pantalla según su categoría (Personas, Vehículos, Animales, Comida, etc.).
* ⚙️ **Panel de Control Dinámico:** Umbral de confianza, resultados máximos, hilos de ejecución y aceleración por hardware (CPU/GPU/NNAPI).
* 🎨 **Diseño Moderno:** Interfaz nativa en español con estética *Dark Glassmorphism*.

---

## 📊 Comparativa y Descubrimientos de Modelos

| Modelo | Tiempo de Inferencia | Precisión | Observaciones |
| :--- | :--- | :--- | :--- |
| **MobileNet SSD V1** | ⚡ Es el más rápido para inferir | ⚠️ Falla algunas veces en detectar personas | Ideal para máximo rendimiento donde la velocidad sea prioritaria. |
| **YOLOv26n** | 🐢 ~45 ms en promedio | 🎯 Muy preciso | Alta precisión de detección, con un tiempo de inferencia casi 4 veces mayor que MobileNet. |
| **YOLOv11n** | ⚡ Rápido y balanceado | 🎯 Alta | Excelente balance entre rendimiento y precisión en tiempo real. |

---

## 🛠️ Requisitos del Sistema

* **Android Studio:** 2024.1+ (Ladybug / Meerkat / 2026+).
* **JDK:** Java 17.
* **SDK Android:** minSdk 26, targetSdk 35.

---

## 📄 Licencia

* **Desarrollado por:** M.C. Emmanuelle Jasso
