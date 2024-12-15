import tensorflow as tf
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image
import numpy as np
import os

# Path ke model
# Path ke model
MODEL_PATH = os.path.join(os.path.dirname(__file__), 'cnn_model.h5')
model = load_model(MODEL_PATH)

# Daftar kelas sampah
CLASS_NAMES = ['cardboard', 'glass', 'metal', 'paper', 'plastic', 'trash']

def predict_waste(img_array):
    """Fungsi untuk melakukan prediksi terhadap gambar yang sudah diproses"""
    try:
        print(f"Input image array shape: {img_array.shape}")  # Debug: Cek bentuk array gambar

        # Melakukan prediksi
        predictions = model.predict(img_array)
        print(f"Prediksi raw: {predictions}")  # Debug: Output prediksi raw

        # Menemukan kelas dengan probabilitas tertinggi
        predicted_class_index = np.argmax(predictions[0])  # Indeks kelas dengan prediksi tertinggi
        predicted_class = CLASS_NAMES[predicted_class_index]  # Menentukan nama kelas
        confidence = predictions[0][predicted_class_index] * 100  # Menghitung confidence dalam persen

        print(f"Kelas yang diprediksi: {predicted_class}, Confidence: {confidence}%")  # Debug hasil prediksi

        return predicted_class, confidence

    except Exception as e:
        print(f"Error during prediction: {str(e)}")
        return None, 0
