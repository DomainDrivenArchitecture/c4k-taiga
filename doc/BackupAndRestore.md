# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Cloud stores files on `/var/jira`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual init the restic repository for the first time

1. Scale backup-restore deployment up:   
   `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/init.sh`
1. Scale backup-restore deployment down:   
  `kubectl scale deployment backup-restore --replicas=0`



## Manual backup the restic repository for the first time

1. Scale gateway and front deployment down:   
  `kubectl scale deployment taiga-gateway-deployment --replicas=0`
  `kubectl scale deployment taiga-front-deployment --replicas=0`
1. Scale backup-restore deployment up:   
   `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/backup.sh`
1. Scale backup-restore deployment down:   
  `kubectl scale deployment backup-restore --replicas=0`
1. Scale gateway and front deployment up:   
   `kubectl scale deployment taiga-gateway-deployment --replicas=1`
  `kubectl scale deployment taiga-front-deployment --replicas=1`


## Manual restore

1. Scale gateway and front deployment down:   
  `kubectl scale deployment taiga-gateway-deployment --replicas=0`
  `kubectl scale deployment taiga-front-deployment --replicas=0`
2. Scale backup-restore deployment up:   
   `kubectl scale deployment backup-restore --replicas=1`
3. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/restore.sh`
4. Scale backup-restore deployment down:   
  `kubectl scale deployment backup-restore --replicas=0`
1. Scale gateway and front deployment up:   
   `kubectl scale deployment taiga-gateway-deployment --replicas=1`
  `kubectl scale deployment taiga-front-deployment --replicas=1`
