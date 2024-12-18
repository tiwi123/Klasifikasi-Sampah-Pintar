from flask import Flask, request, jsonify
import numpy as np
from PIL import Image
import io
from tensorflow.keras.models import load_model
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# Aktifkan CORS untuk frontend tertentu (gantilah sesuai dengan domain frontend Anda)
CORS(app, resources={r"/classify": {"origins": "http://localhost:3000"}})

# Muat model (pastikan path model benar)
try:
    model = load_model('model/cnn_model1.h5')
    print("Model berhasil dimuat.")
except Exception as e:
    print(f"Error loading model: {e}")
    model = None

# Kelas untuk klasifikasi
CLASS_LABELS = ["Organik", "Anorganik"]  # Indeks 0 = Organik, Indeks 1 = Anorganik

@app.route('/')
def home():
    return "Server is running!"

@app.route('/classify', methods=['POST'])
def classify():
    # Periksa apakah model sudah dimuat
    if model is None:
        return jsonify({'error': 'Model is not loaded properly'}), 500

    # Periksa apakah ada file gambar yang diunggah
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    file = request.files['image']

    # Periksa apakah file memiliki nama
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    # Validasi format file gambar
    ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}
    if not ('.' in file.filename and file.filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS):
        return jsonify({'error': 'Invalid image format. Please upload PNG, JPG, or JPEG image.'}), 400

    try:
        # Membaca gambar dari file yang diunggah menggunakan PIL
        img = Image.open(io.BytesIO(file.read()))
        img = img.convert('RGB')  # Pastikan gambar dalam format RGB

        # Resize gambar ke ukuran yang sesuai dengan input model
        img = img.resize((224, 224))

        # Konversi gambar menjadi array numpy
        img_array = np.array(img) / 255.0  # Normalisasi gambar

        # Tambahkan dimensi batch (karena model membutuhkan input batch)
        img_array = np.expand_dims(img_array, axis=0)

        # Prediksi
        predictions = model.predict(img_array)
        class_idx = int(np.argmax(predictions, axis=1)[0])
        confidence = float(predictions[0][class_idx])

        # Mapping ke label kelas
        predicted_class = CLASS_LABELS[class_idx]

        # Kembalikan hasil klasifikasi
        return jsonify({
            'class': predicted_class,
            'confidence': confidence
        }), 200

    except Exception as e:
        return jsonify({'error': f"Error processing image: {str(e)}"}), 500

if __name__ == '__main__':
    # Pastikan server dapat diakses dari jaringan lain
    app.run(host='0.0.0.0', port=5000, debug=True)
