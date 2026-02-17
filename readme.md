# Events API

Spring Boot -pohjainen REST API tapahtumien hallintaan. Projekti sisältää konfiguraatioprofiilit (dev / test / prod), automaattiset testit, Docker-konttipaketoinnin ja CI/CD-putken GitHub Actionsilla. Tuotantodeploy tapahtuu CSC cPouta -virtuaalikoneelle.

---

## Sisällysluettelo

1. [Arkkitehtuuri](#arkkitehtuuri)
2. [Teknologiat](#teknologiat)
3. [Paikallinen kehitys (Docker Desktop)](#paikallinen-kehitys-docker-desktop)
4. [Konfiguraatioprofiilit](#konfiguraatioprofiilit)
5. [REST API -rajapinta](#rest-api--rajapinta)
6. [Testaus](#testaus)
7. [CI/CD-putki](#cicd-putki)
8. [Tuotantoympäristö (CSC cPouta)](#tuotantoympäristö-csc-cpouta)
9. [GitHub Secrets](#github-secrets)

---

## Arkkitehtuuri

```
┌──────────────┐       ┌──────────────────┐       ┌────────────┐
│   Client     │──────▶│  Spring Boot API  │──────▶│ PostgreSQL │
│  (selain /   │ HTTP  │  (port 8080)      │  JPA  │            │
│   curl)      │◀──────│                   │◀──────│            │
└──────────────┘       └──────────────────┘       └────────────┘
```

Sovellus noudattaa perinteistä kolmikerrosarkkitehtuuria:

| Kerros       | Luokka                | Vastuu                        |
| ------------ | --------------------- | ----------------------------- |
| Controller   | `EventController`     | HTTP-pyyntöjen käsittely      |
| Repository   | `EventRepository`     | Tietokantaoperaatiot (JPA)    |
| Model        | `Event`               | Entiteetti / tietomalli       |

---

## Teknologiat

| Komponentti      | Valinta                     |
| ---------------- | --------------------------- |
| Kieli            | Java 21 (LTS)               |
| Framework        | Spring Boot 3.5.0           |
| Tietokanta       | PostgreSQL 16               |
| ORM              | Hibernate / Spring Data JPA |
| Testit           | JUnit 5, MockMvc, H2        |
| Kontit           | Docker, Docker Compose       |
| CI/CD            | GitHub Actions              |
| Container Registry | Docker Hub                |
| Tuotanto         | CSC cPouta (Ubuntu VM)       |

---

## Paikallinen kehitys (Docker Desktop)

> **Ohje uudelle kehittäjälle** – pääset käyntiin ilman suullista ohjausta.

### Esitiedot

- [Git](https://git-scm.com/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Käynnistys

```bash
# 1. Kloonaa repositorio
git clone https://github.com/<käyttäjä>/csc-ci.git
cd csc-ci

# 2. Käynnistä ympäristö
docker compose up --build
```

### Mitä käynnistyy?

| Palvelu | Kontti             | Portti         | Kuvaus                      |
| ------- | ------------------ | -------------- | --------------------------- |
| app     | events-app-dev     | localhost:8080 | Spring Boot API (dev-profiili) |
| db      | events-db-dev      | localhost:5432 | PostgreSQL-tietokanta        |

### Testaa API:a

```bash
# Luo tapahtuma
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"name":"Seminaari","date":"2026-03-15","location":"Helsinki"}'

# Listaa tapahtumat
curl http://localhost:8080/api/events
```

### Pysäytä ympäristö

```bash
docker compose down          # pysäyttää kontit
docker compose down -v       # pysäyttää ja poistaa tietokantavolyymin
```

---

## Konfiguraatioprofiilit

Spring Boot tukee profiilipohjaista konfiguraatiota. Jokaiselle ympäristölle on oma `application-<profiili>.yml`-tiedosto.

### Profiilien yhteenveto

| Profiili | Tiedosto                  | Tietokanta         | DDL-strategia  | Käyttötarkoitus            |
| -------- | ------------------------- | ------------------ | -------------- | -------------------------- |
| `dev`    | `application-dev.yml`     | PostgreSQL (Docker) | `update`      | Paikallinen kehitys        |
| `test`   | `application-test.yml`    | H2 in-memory       | `create-drop` | Automaattiset testit       |
| `prod`   | `application-prod.yml`    | PostgreSQL (Docker) | `update`      | Tuotanto CSC cPouta        |

### Profiilin aktivointi

```bash
# Komentoriviltä
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Dockerissa (ympäristömuuttuja)
docker run -e SPRING_PROFILES_ACTIVE=prod events-api

# Docker Composessa (docker-compose.yml)
environment:
  SPRING_PROFILES_ACTIVE: dev
```

### Dev-profiili (`application-dev.yml`)

- PostgreSQL osoitteessa `localhost:5432/eventsdb`
- Käyttäjä `devuser` / `devpass`
- SQL-lokitus päällä (DEBUG-taso)

### Test-profiili (`application-test.yml`)

- **H2 in-memory** -tietokanta – ei vaadi ulkoista tietokantaa
- `create-drop` – skeema luodaan alusta jokaisen testiajon alussa
- Nopea ja toistettava

### Prod-profiili (`application-prod.yml`)

- PostgreSQL-yhteys ympäristömuuttujista (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- Minimaalinen lokitus (WARN/INFO)
- Arkaluontoiset arvot eivät ole lähdekoodissa

---

## REST API -rajapinta

Base URL: `http://localhost:8080/api/events`

| Metodi   | Polku              | Kuvaus                | Req Body | Vastaus         |
| -------- | ------------------ | --------------------- | -------- | --------------- |
| `GET`    | `/api/events`      | Listaa kaikki         | –        | `200` + JSON[]  |
| `GET`    | `/api/events/{id}` | Hae yksittäinen       | –        | `200` / `404`   |
| `POST`   | `/api/events`      | Luo uusi tapahtuma    | JSON     | `201` + JSON    |
| `PUT`    | `/api/events/{id}` | Päivitä tapahtuma     | JSON     | `200` / `404`   |
| `DELETE` | `/api/events/{id}` | Poista tapahtuma      | –        | `204` / `404`   |

### Event JSON -malli

```json
{
  "id": 1,
  "name": "Seminaari",
  "date": "2026-03-15",
  "location": "Helsinki"
}
```

### Validointi

- `name` – pakollinen, ei saa olla tyhjä
- `date` – pakollinen
- `location` – pakollinen, ei saa olla tyhjä

Virheellinen pyyntö palauttaa `400 Bad Request`.

---

## Testaus

### Miksi erillinen testiprofiili?

Erillinen testiprofiili on olennainen osa ammattimaista ohjelmistotuotantoa:

- **Eristys** – testit eivät vaikuta kehitys- tai tuotantotietokantaan
- **Toistettavuus** – H2 in-memory -kanta luodaan puhtaana jokaisessa ajossa
- **Nopeus** – muistipohjainen tietokanta on huomattavasti nopeampi
- **CI-yhteensopivuus** – testit toimivat ilman ulkoisia riippuvuuksia

### Testien rakenne

| Testitiedosto              | Kuvaus                              |
| -------------------------- | ----------------------------------- |
| `EventsApplicationTests`   | Spring-kontekstin latautuminen      |
| `EventControllerTest`      | CRUD-operaatioiden integraatiotestit |

Testit kattavat:
- Kaikkien tapahtumien listaus (tyhjä + data)
- Yksittäisen tapahtuman haku (löytyy + ei löydy)
- Tapahtuman luonti (onnistuu + validointivirhe)
- Tapahtuman päivitys (onnistuu + ei löydy)
- Tapahtuman poisto (onnistuu + ei löydy)

### Testien ajaminen

```bash
# Paikallisesti
./mvnw test -Dspring.profiles.active=test

# Tai annotaatiolla (testeissä on @ActiveProfiles("test"))
./mvnw test
```

---

## CI/CD-putki

GitHub Actions -putki suorittaa kolme vaihetta aina kun `main`-haaraan pusketaan koodia.

### Putken vaiheet

```
push main
  │
  ▼
┌─────────────┐    ┌───────────────────┐    ┌──────────────────┐
│  1. TEST    │───▶│ 2. BUILD & PUSH   │───▶│ 3. DEPLOY        │
│  (mvnw test)│    │ (Docker Hub)      │    │ (SSH → cPouta)   │
│  test-profiili   │ image:latest +    │    │ docker compose   │
│             │    │ image:<sha>       │    │ pull + up -d     │
└─────────────┘    └───────────────────┘    └──────────────────┘
```

1. **Test** – ajaa automaattiset testit `test`-profiililla (H2)
2. **Build & Push** – rakentaa Docker-imagen ja julkaisee Docker Hubiin
3. **Deploy** – kopioi `docker-compose.prod.yml` CSC-palvelimelle SSH:lla ja käynnistää palvelut

Workflow-tiedosto: `.github/workflows/ci-cd.yml`

### Pull request -käsittely

Pull requesteissa suoritetaan ainoastaan testit (vaihe 1). Build ja deploy tapahtuvat vain `main`-haaraan mergatessa.

---

## Tuotantoympäristö (CSC cPouta)

### Esitiedot virtuaalikoneella

- Ubuntu (CSC cPouta VM)
- Docker ja Docker Compose asennettu
- Palomuurissa TCP-portti 8080 auki

### Arkkitehtuuri tuotannossa

```
CSC cPouta VM
├── docker-compose.prod.yml
├── events-app-prod   (Spring Boot, prod-profiili)
└── events-db-prod    (PostgreSQL 16)
```

API vastaa osoitteessa: `http://<VM-IP>:8080/api/events`

### Manuaalinen käynnistys (tarvittaessa)

```bash
ssh -i avain.pem ubuntu@<VM-IP>
cd ~/events-api

export DOCKER_IMAGE=<dockerhub-user>/events-api:latest
export DB_NAME=eventsdb
export DB_USERNAME=produser
export DB_PASSWORD=prodpass

docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

---

## GitHub Secrets

CI/CD-putki tarvitsee seuraavat salaisuudet repositorion asetuksissa (**Settings → Secrets and variables → Actions**):

| Secret              | Kuvaus                                      |
| ------------------- | ------------------------------------------- |
| `DOCKERHUB_USERNAME`| Docker Hub -käyttäjätunnus                  |
| `DOCKERHUB_TOKEN`   | Docker Hub Access Token                     |
| `HOST`              | CSC cPouta VM:n IP-osoite                   |
| `USERNAME`          | SSH-käyttäjä (esim. `ubuntu`)               |
| `KEY`               | SSH-yksityisavain deployta varten            |
| `DB_NAME`           | Tuotantotietokannan nimi (esim. `eventsdb`) |
| `DB_USERNAME`       | Tuotantotietokannan käyttäjä                |
| `DB_PASSWORD`       | Tuotantotietokannan salasana                |

### SSH-avaimen luominen deploylle

```bash
ssh-keygen -t ed25519 -f deploy_key -N ""
# deploy_key.pub → lisää VM:lle (~/.ssh/authorized_keys)
# deploy_key     → lisää GitHub Secretsiin (KEY)
```