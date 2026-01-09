
# GitHub SSH-avaimen lisääminen salaisuudeksi

## Vaihe 1: SSH-avaimen luominen
```bash
ssh-keygen -t ed25519 -f deploy_key -N ""
```
Tämä luo kaksi tiedostoa: `deploy_key` (yksityinen) ja `deploy_key.pub` (julkinen).

## Vaihe 2: Julkisen avaimen lisääminen virtuaalikoneelle
```bash
cat deploy_key.pub >> ~/.ssh/authorized_keys
```

## Vaihe 3: Yksityisen avaimen lisääminen GitHub Secretsiksi
1. Siirry repositorion **Settings** → **Secrets and variables** → **Actions**
2. Klikkaa **New repository secret**
3. Nimi: `DEPLOY_SSH_KEY`
4. Arvo: Kopioi `deploy_key` -tiedoston sisältö
5. Klikkaa **Add secret**

## Vaihe 4: GitHub Actions workflow
Luo `.github/workflows/deploy.yml`:
```yaml
name: Deploy HTML

on: [push]

jobs:
    deploy:
        runs-on: ubuntu-latest
        steps:
            - uses: actions/checkout@v3
            
            - name: Deploy
                env:
                    SSH_KEY: ${{ secrets.DEPLOY_SSH_KEY }}
                run: |
                    mkdir -p ~/.ssh
                    echo "$SSH_KEY" > ~/.ssh/deploy_key
                    chmod 600 ~/.ssh/deploy_key
                    ssh-keyscan -H your-vm-ip >> ~/.ssh/known_hosts
                    scp -i ~/.ssh/deploy_key -r ./index.html user@your-vm-ip:/var/www/html/
```

Korvaa `your-vm-ip` ja `user` omilla arvoillasi.
