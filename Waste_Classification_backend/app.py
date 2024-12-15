import os
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
from model.waste_model import predict_waste  # Function to predict from the model
import numpy as np
from PIL import Image

app = Flask(__name__)

# Folder for storing uploaded images
UPLOAD_FOLDER = 'static/uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}  # Allowed file formats
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Ensure the upload folder exists
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    """Check if the file has an allowed extension."""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def preprocess_image(image_path):
    """Load and preprocess the image for prediction."""
    try:
        img = Image.open(image_path)
        img = img.resize((224, 224))  # Resize to the appropriate size for the model
        img = np.array(img) / 255.0  # Normalize the image
        img = np.expand_dims(img, axis=0)  # Add batch dimension
        print(f"Image after preprocessing: {img.shape}")  # Check image dimensions
        return img
    except Exception as e:
        print(f"Error in preprocessing image: {str(e)}")
        return None

@app.route('/')
def home():
    return "Flask server is running. Use the '/api/classify' endpoint to classify images."

@app.route('/api/classify', methods=['POST'])
def classify_waste():
    # Check if a file is in the request
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    if not allowed_file(file.filename):
        return jsonify({'error': 'File type not allowed. Only PNG, JPG, JPEG are accepted.'}), 400

    try:
        # Save the uploaded image file
        filename = secure_filename(file.filename)
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)

        print(f"File saved to: {filepath}")

        # Preprocess the image before prediction
        img_array = preprocess_image(filepath)
        if img_array is None:
            return jsonify({'error': 'Error processing image'}), 500

        print(f"Image array shape before prediction: {img_array.shape}")

        # Perform prediction with the model
        try:
            predicted_class, confidence = predict_waste(img_array)
        except Exception as pred_error:
            print(f"Error during prediction: {str(pred_error)}")
            return jsonify({'error': 'Prediction failed'}), 500

        print(f"Predicted class: {predicted_class}, Confidence: {confidence}")

        if predicted_class is None:
            return jsonify({'error': 'Prediction failed'}), 500

        # Remove the file after prediction
        try:
            os.remove(filepath)
            print(f"File {filepath} removed after prediction.")
        except Exception as remove_error:
            print(f"Error removing file: {str(remove_error)}")

        # Return the prediction results
        return jsonify({
            'class': predicted_class,
            'confidence': float(confidence)
        })

    except Exception as e:
        print(f"Error during file processing: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)
