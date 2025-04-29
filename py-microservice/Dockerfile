FROM python:3.10-slim
WORKDIR /app
COPY . .

# Install dependencies, including PyTorch
RUN pip install --no-cache-dir -r requirements.txt \
    && pip install torch

EXPOSE 5000
CMD ["python", "main.py"]
