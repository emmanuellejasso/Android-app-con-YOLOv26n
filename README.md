# Detección de Distancia de Peatones con YOLO26 🚀

**Detección de Distancia de Peatones con YOLO26** es una aplicación móvil de visión por computadora para Android desarrollada para realizar **detección exclusiva de personas, estimación de pose y cálculo de distancia en tiempo real** utilizando la cámara en vivo y modelos de inteligencia artificial como **Ultralytics YOLOv26n** y **TensorFlow Lite**.

---

## 👨‍💻 Creador y Desarrollador
* **Autor:** M.C. Emmanuelle Jasso
* **Proyecto:** Visión por Computadora & Detección de Distancia de Peatones en Android

---

## ✨ Características Principales

* 👤 **Detección Exclusiva de Peatones:** Identifica en tiempo real únicamente a personas en el flujo de la cámara (filtrando cualquier otra categoría no deseada).
* 📏 **Algoritmo de Distancia (Cintura a Cabeza 38%):** Calcula la distancia óptica en metros ($D = \frac{f_y \cdot H_{cintura\_cabeza}}{h_{segmento\_px}}$) midiendo el segmento de la cintura a la cabeza (38% de la estatura total promedio de 1.65 m = **0.627 m**).
* 🦴 **Switch de Estimación de Pose:** Botón conmutable para habilitar en tiempo real la superposición gráfica del esqueleto corporal y la línea de cintura de referencia.
* ⚡ **Switch de Modo Alta Velocidad:**
  * **YOLOv26n (Predeterminado):** Máxima precisión para detección de peatones (~45 ms).
  * **MobileNet SSD V1 (Activado):** Máxima velocidad de inferencia (~11 ms).
* ⚙️ **Panel de Control Dinámico:** Umbral de confianza, resultados máximos, hilos de ejecución y aceleración por hardware (CPU/GPU/NNAPI).

---

## 📊 Comparativa de Modelos

| Modelo | Tiempo de Inferencia | Precisión | Uso Recomendado |
| :--- | :--- | :--- | :--- |
| **YOLOv26n** (Predeterminado) | ~45 ms promedio | 🎯 **Muy alta** | Análisis de precisión para detección constante de personas. |
| **MobileNet SSD V1** (Modo Alta Velocidad) | ⚡ ~11 ms promedio | 🚀 **Ultra rápido** | Mayor fluidez de FPS cuando la velocidad es la prioridad. |

---

## 🛠️ Requisitos del Sistema

* **Android Studio:** 2024.1+ (Ladybug / Meerkat / 2026+).
* **JDK:** Java 17.
* **SDK Android:** minSdk 26, targetSdk 35.

---

## 📄 Licencia

* **Desarrollado por:** M.C. Emmanuelle Jasso
