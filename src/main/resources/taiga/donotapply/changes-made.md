Comment EVENTS_PUSH_BACKEND_URL in taiga-events-deployment
Indent name fields in envFrom field in taiga-back-deployment
Remove indentation from name field in taiga-gateway-deployment
Switch name and mountPath field positions in taiga-gateway-deployment
Change postres to 1Gi in pvc.yaml
b64 encoded values in *-secret.yaml
Change integers to strings in env vars in deployments and configmaps
Change bools to strings in env vars in deployments and configmaps
Increase storage to 8Gi in pvc.yaml
Change storageClassName to local-path in pvc.yaml
Correct volume names in async, back, gateway
Use service name as address in taiga-gateway-configmap.yaml
Correct reference to taiga-configmap and taiga-secret in taiga-back-deployment
Remove init-container in taiga-back-deployment
Update command in taiga-back-deployment to ["/taiga-back/docker/entrypoint.sh"]
Update command in taiga-back-deployment to command: ["/taiga-back/docker/entrypoint.sh && python manage.py createsupersuer"]
Extend configmap in taiga-config map by values for taiga-front # we may want to check CAPITALIZATION of KW before starting work in c4k code
Rename taiga-async-rabbitmq-service to taiga-async-rabbitmq
Move erlang cookie to taiga-rabbitmq-secret in taiga-async-rabbitmq-deployment
Change value of RABBITMQ_DEFAULT_VHOST to taiga in taiga-async-rabbitmq-deployment
Change value of RABBITMQ_USER in taiga-secret.yaml to b64/encode taiga
Change value of RABBITMQ_DEFAULT_USER in taiga-rabbitmq-secret.yaml to b64/encode taiga
Remove -service suffix from all taiga service names
Remove -service suffix from all urls in taiga-gateway configmap
Remove -service suffix from ingress
Add - name: RABBITMQ_LOGS value: /opt/rabbitmq/logs.log in taiga-async-rabbitmq-deployment
Get RABBITMQ_ERLANG_COOKIE from taiga-secret in taiga-events-rabbitmq-deployment
Put RABBITMQ_DEFAULT_VHOST KV pair in taiga-configmap
Get RABBITMQ_DEFAULT_VHOST from taiga-configmap in taiga-events-rabbitmq-deployment
Get RABBITMQ_DEFAULT_VHOST from taiga-configmap in taiga-async-rabbitmq-deployment
Move all values from taiga-rabbitmq-secret to taiga-secret
Remove taiga-rabbitmq-secret from config
Rename all occurrences of taiga-rabbitmq-secret to taiga-secret
Add SESSION_COOKIE_SECURE: "False" and CSRF_COOKIE_SECURE: "False" to taiga-configmap.yaml