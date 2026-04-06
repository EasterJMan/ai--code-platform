# RocketMQ Docker Scripts

## Files

- `docker-compose.yml`: RocketMQ (NameServer + Broker + Dashboard)
- `deploy.sh`: one-click deploy
- `start.sh`: start services
- `stop.sh`: stop services
- `uninstall.sh`: remove services

## Usage

```bash
cd scripts/rocketmq
chmod +x *.sh
# 默认低内存模式（不启动 dashboard）
./deploy.sh
./start.sh
./stop.sh
./uninstall.sh
```

## deploy.sh Arguments

1. image (default: `apache/rocketmq:5.3.2`)
2. namesrv container (default: `rmq-namesrv`)
3. broker container (default: `rmq-broker`)
4. namesrv port (default: `9876`)
5. broker port (default: `10911`)
6. broker ha port (default: `10909`)
7. broker fast port (default: `10912`)
8. proxy port (default: `8081`)
9. broker java opt (default: `-server -Xms384m -Xmx384m -Xmn192m`)
10. dashboard image (default: `apacherocketmq/rocketmq-dashboard:latest`)
11. dashboard container (default: `rmq-dashboard`)
12. dashboard port (default: `8080`)
13. enable dashboard (default: `false`)
14. namesrv java opt (default: `-server -Xms128m -Xmx128m -Xmn64m`)

Example:

```bash
./deploy.sh apache/rocketmq:5.3.2 rmq-namesrv rmq-broker 9876 10911 10909 10912 8081 "-server -Xms384m -Xmx384m -Xmn192m" apacherocketmq/rocketmq-dashboard:latest rmq-dashboard 8080 true "-server -Xms128m -Xmx128m -Xmn64m"
```

Dashboard URL:

- `http://<server-ip>:8080`

Enable dashboard at startup:

```bash
./deploy.sh apache/rocketmq:5.3.2 rmq-namesrv rmq-broker 9876 10911 10909 10912 8081 "-server -Xms384m -Xmx384m -Xmn192m" apacherocketmq/rocketmq-dashboard:latest rmq-dashboard 8080 true "-server -Xms128m -Xmx128m -Xmn64m"
./start.sh true
```
