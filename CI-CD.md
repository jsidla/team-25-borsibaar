# ssh server

## General team-25 server

- IP `193.40.157.118`
- Kasutajanimi: ubuntu
- Ressursid: 2GB RAM, 1 vCPU
- Avatud pordid: ssh, pint, http, https

## jsidla personal server

- Created with Azure for Students
- IP `172.160.225.155`
- Ressursid: 4GB RAM, 2 vCPU

# Tutorial of setting up self hosted runner
- https://github.com/taltech-vanemarendajaks/Examples/tree/main/setting-up-self-hosted-runner

# Pre task assignments
- [x] Fork the repo (this is needed to complete task 4) ([link](https://github.com/jsidla/team-25-borsibaar))
- [x] Create a VPS (virtual machine)

# Step 1: Server Setup

- [x] SSH into your server
```
cd ~/Downloads
chmod 600 borsibaar-vm-suur_key.pem
ssh -i borsibaar-vm-suur_key.pem azureuser@172.160.225.155
```
<!-- Failed to upload "image.png" -->

- [x] Install Docker
```
# Serveris
sudo apt update # This refreshes the list of available packages and versions from the repositories configured on your system
sudo apt upgrade -y # This upgrades all installed packages to the latest versions available in the repositories

# Installi Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Lisa kasutaja docker gruppi
sudo usermod -aG docker $USER

# Logi välja ja uuesti sisse
exit

# SSH uuesti serverisse
ssh -i ~/Downloads/borsibaar-vm-suur_key.pem azureuser@172.160.225.155

# Testi Dockeri olemasolu
docker --version
docker ps
```
<img width="508" height="74" alt="Image" src="https://github.com/user-attachments/assets/6f1cec89-4cf4-4653-b9a0-5d13a40088c2" />

- [x] Install NGINX
```
# Serveris:
sudo apt install nginx -y

sudo systemctl start nginx
sudo systemctl enable nginx

sudo systemctl status nginx
# Vajuta 'q'
```
<img width="890" height="219" alt="Image" src="https://github.com/user-attachments/assets/0d593db8-43b1-4b8e-bb75-10b019db7df0" />

- [x] Test: http://172.160.225.155
<img width="831" height="303" alt="Image" src="https://github.com/user-attachments/assets/dadadc21-03da-460a-92ce-237097702299" />

# Step 2: Manual Deployment

## Build backend

- [x] Build backend locally

```
cd backend && ./mvnw clean package
cd .. && docker build -t borsibaar-backend:v1 ./backend
```
<img width="842" height="244" alt="Image" src="https://github.com/user-attachments/assets/d521df2a-fd93-4e64-b683-5fb62883551d" />

- [x] Create Docker image

```
docker login
docker tag borsibaar-backend:v1 jsidla/borsibaar-backend:v1
docker push jsidla/borsibaar-backend:v1
```
<img width="782" height="159" alt="Image" src="https://github.com/user-attachments/assets/6d9e8185-12a8-410a-97da-96fbf097377f" />

- [x] Copy to server and run
```
# Serveris:
sudo mkdir -p /opt/borsibaar
sudo chown azureuser:azureuser /opt/borsibaar
cd /opt/borsibaar

nano docker-compose.yml
```
Lisa Docker compose faili sisu sinna -> Ctrl+O, Enter, Ctrl+X

- [x] Verify it works

```
docker compose up -d
docker compose logs -f backend
```
- [x] Test: http://172.160.225.155:8080
<img width="1313" height="147" alt="Image" src="https://github.com/user-attachments/assets/72167607-7701-4f51-84c7-36cec7f7246b" />

## Build frontend

- [x] Build frontend locally
```
cd frontend
npm install
npm run build
```

- [x] Copy to server and run

First, locally:
```
# Package the `frontend/` directory into a file called `frontend-full.tar.gz`
cd ..
tar -czf frontend-full.tar.gz frontend/

# Securely copy the local folder to the server using SSH
scp -i ~/Downloads/borsibaar-vm-suur_key.pem frontend-full.tar.gz azureuser@172.160.225.155:/opt/borsibaar/
```

Then, in the server:
```
# Unpack the files
cd /opt/borsibaar
tar -xzf frontend-full.tar.gz

# Install Node.js and verify
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node --version

# Go to the `/frontend` directory and run on the server
cd /opt/borsibaar/frontend
npm run build
node .next/standalone/server.js > frontend.log 2>&1 & 
```

NGINX reverse proxy:
```
sudo nano /etc/nginx/sites-available/default
```
Add this content to the file:
```
server {
    listen 80;
    server_name jsidla.zapto.org;

    # Serve static assets
    location /_next/static/ {
        alias /opt/borsibaar/frontend/.next/static/;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
-> Ctrl+O, Enter, Ctrl+X

Test and reload Nginx:
```
sudo nginx -t
sudo systemctl restart nginx
```
<img width="475" height="75" alt="Image" src="https://github.com/user-attachments/assets/93184771-ada6-4a1f-a4c0-b1c94026d6ff" />

- [x] Verify it works

```
tail -f frontend.log
```
<img width="525" height="120" alt="Image" src="https://github.com/user-attachments/assets/ff72957e-c834-4022-a894-44085a879127" />

```
curl -I http://localhost:3000
```
<img width="732" height="233" alt="Image" src="https://github.com/user-attachments/assets/ef05954f-d51e-437b-bac6-9b9627ea9eac" />

# Step 3: Domain

- [x] Register (free) domain (noip.com) - jsidla.zapto.org
- [x] Add A record pointing to server IP
- [x] Access your app via domain name
<img width="532" height="31" alt="Image" src="https://github.com/user-attachments/assets/3690f4a3-0fdf-44f8-9aea-4f11ec9c1f5d" />
<img width="1194" height="784" alt="Image" src="https://github.com/user-attachments/assets/eecb8c84-8cc0-4ec9-ac17-ff3c9ced05fa" />

# Step 4: CI/CD

- [x] Add GitHub secrets (SSH key, server IP, etc.)
- [x] Bonus: modify to use Docker registry instead of tar.gz
- [x] Get existing workflow working

# Step 5: HTTPS (if available)

- [x] Install Certbot
```
# Update packages
sudo apt update

# Install Certbot
sudo apt install certbot -y

# Stop Nginx
sudo systemctl stop nginx

# Verify port 80 is free
sudo lsof -i :80
```

- [x] Get certificate for your domain
```
# Get the certificate
sudo certbot certonly --standalone \
  -d jsidla.zapto.org \
  --email johannastina.idla@gmail.com \
  --agree-tos \
  --non-interactive
```
<img width="671" height="303" alt="Image" src="https://github.com/user-attachments/assets/2a2c8cde-cdfd-4ecf-bcb7-cd72e08efc75" />

- [x] Verify it works: Test https://jsidla.zapto.org/ 
```
# Create a new nginx conf
nano nginx.conf
```
Add this:
```
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Redirect HTTP to HTTPS
    server {
        listen 80;
        server_name jsidla.zapto.org;
        
        location /.well-known/acme-challenge/ {
            root /var/www/certbot;
        }
        
        location / {
            return 301 https://$host$request_uri;
        }
    }

    # HTTPS server
    server {
        listen 443 ssl http2;
        server_name jsidla.zapto.org;

        ssl_certificate /etc/letsencrypt/live/jsidla.zapto.org/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/jsidla.zapto.org/privkey.pem;
        
        # SSL configuration
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;
        ssl_prefer_server_ciphers on;

        # Frontend
        location / {
            proxy_pass http://frontend:3000;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Backend API
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # WebSocket support
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }
    }
}
```
```
# Restart containers with new configuration
docker compose -f docker-compose.prod.yaml down
docker compose -f docker-compose.prod.yaml up -d

# Test from command line
curl -I https://jsidla.zapto.org
```
Test on local machine:
<img width="791" height="564" alt="Image" src="https://github.com/user-attachments/assets/8736409f-7f46-4641-9f60-c3c135badc24" />

✅