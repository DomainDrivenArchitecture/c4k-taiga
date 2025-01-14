# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Cloud stores files on `/var/jira`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual backup

1. Scale gateway and front deployment down:   
   `kubectl -n taiga scale deployment taiga-gateway-deployment --replicas=0`
   `kubectl -n taiga scale deployment taiga-front-deployment --replicas=0`
2. Scale backup-restore deployment up:   
   `kubectl -n taiga scale deployment backup-restore --replicas=1`
3. exec into pod and execute restore pod   
   `kubectl -n taiga exec -it backup-restore -- backup.bb`
4. Scale backup-restore deployment down:   
  `kubectl -n taiga scale deployment backup-restore --replicas=0`
1. Scale gateway and front deployment up:   
   `kubectl -n taiga scale deployment taiga-gateway-deployment --replicas=1`
   `kubectl -n taiga scale deployment taiga-front-deployment --replicas=1`

## Manual restore

1. Scale gateway and front deployment down:   
   `kubectl -n taiga scale deployment taiga-gateway-deployment --replicas=0`
   `kubectl -n taiga scale deployment taiga-front-deployment --replicas=0`
2. Scale backup-restore deployment up:   
   `kubectl -n taiga scale deployment backup-restore --replicas=1`
3. exec into pod and execute restore pod   
   `kubectl -n taiga exec -it backup-restore -- restore.bb`
4. Scale backup-restore deployment down:   
  `kubectl -n taiga scale deployment backup-restore --replicas=0`
5. Scale gateway and front deployment up:   
   `kubectl -n taiga scale deployment taiga-gateway-deployment --replicas=1`
   `kubectl -n taiga scale deployment taiga-front-deployment --replicas=1`

## Change Password

1. Check restic-new-password env is set in backup deployment   
   ```
   kind: Deployment
   metadata:
     name: backup-restore
   spec:
       spec:
         containers:
         - name: backup-app
           env:
           - name: RESTIC_NEW_PASSWORD_FILE
             value: /var/run/secrets/backup-secrets/restic-new-password
   ```
2. Add restic-new-password to secret   
   ```
   kind: Secret
   metadata:
     name: backup-secret
   data:
     restic-password: old
     restic-new-password: new
   ```
3. Scale backup-restore deployment up:   
   `kubectl -n taiga scale deployment backup-restore --replicas=1`
4. exec into pod and execute restore pod   
   `kubectl -n taiga exec -it backup-restore -- change-password.bb`
5. Scale backup-restore deployment down:   
  `kubectl -n taiga scale deployment backup-restore --replicas=0`
6. Replace restic-password with restic-new-password in secret   
   ```
   kind: Secret
   metadata:
     name: backup-secret
   data:
     restic-password: new
   ```