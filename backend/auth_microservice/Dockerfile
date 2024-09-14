# Use official Python image as the base image
FROM python:3.10-slim

ARG DIRNAME=auth_microservice

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Set the working directory in the container
WORKDIR /app

# Copy only the dependencies file to optimize rebuilding the Docker image
COPY ./$DIRNAME/pyproject.toml ./


# Install Poetry and project dependencies
RUN pip install --no-cache-dir poetry && \
    poetry config virtualenvs.create false && \
    poetry install --no-dev --no-interaction --no-ansi

# Copy gen_utils package
COPY ./gen_utils ./gen_utils
# Copy the rest of the application code
COPY ./$DIRNAME .

# Command to run the application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]