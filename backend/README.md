# Backend Microservices


## Prerequisites:

- docker / docker-compose
- make
- setup .env file (copy paste from discord and place it in ./backend/.env) DO NOT PUSH THIS FILE!

# Installation / Running

To install / build the services run the following command

```make all```

The following services are built & deployed.

- PostgreSQL
- Redis
- FastAPI Microservice1
- FastAPI Microservice2
- FastAPI Microservice3

Rename these as the microservices evolve / templates are changed. Each microservice is already configured to connect to the database and redis instance, with a dummy fastapi endpoint. Connect to [f"http://localhost:800{num}" for num in {1,2,3}] to see an example of this working.

New packages can be installed in the respective pyproject.toml files.

# Cleaning
To remove all the built images / containers run the following command. Note that this _WILL_ delete all data in the database!

```make clean```