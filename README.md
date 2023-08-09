# convention 4 kubernetes: c4k-taiga

[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-taiga.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-taiga) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-taiga/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-taiga/-/commits/main) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [taiga & Blog](https://domaindrivenarchitecture.org)

## Configuration Issues

https://github.com/kaleidos-ventures/taiga-docker
https://community.taiga.io/t/taiga-30min-setup/170

Note: taiga-manage,-back und -async verwenden die gleichen docker images mit unterschiedlichen entry-points.

### HTTPS

Terminiert am ingress. Wie interagiert das mit taiga?
Eventuell wird dies hier relevant:
https://github.com/kaleidos-ventures/taiga-docker#session-cookies-in-django-admin

### Docker Compose -> Kubernetes

Wir müssen die compose-yamls nach kubernetes resources übersetzen.  

### Für das init deployment

Es gibt einen Init-Container mit namen *taiga-manage* im deployment.
ToDo: Dieser erstellt einen Admin User mit credentials aus dem taiga-back-secret.

#### Einen admin-user anlegen:
https://github.com/kaleidos-ventures/taiga-docker#configure-an-admin-user

folglich:

https://docs.djangoproject.com/en/4.2/ref/django-admin/#django-admin-createsuperuser

Also DJANGO_SUPERUSER_TAIGAADMIN und DJANGO_SUPERUSER_PASSWORD
sollten für den Container gesetzt sein.

Dann noch ein run befehl mit: python manage.py createsuperuser im init container unterbringen.

### deployment

Taiga reads many values in config.py from env vars as can be seen in the taiga-back [config.py](
https://github.com/kaleidos-ventures/taiga-back/blob/main/docker/config.py). These are read from configmaps and secrets 
in the deployment.

Mounting a configmap with a config.py as described here: https://docs.taiga.io/setup-production.html could be interesting. A mix of both env-vars and config.py in one container is not possible.

An example for a config.py is given here:
https://github.com/kaleidos-ventures/taiga-back/blob/main/settings/config.py.prod.example 

* taiga-db
* Postgres
* taiga-back
* taiga-async
* taiga-async-rabbitmq
* taiga-front
* taiga-events
* taiga-events-rabbitmq
* taiga-protected
* taiga-gateway
  * Nginx???
  * ersetzen durch metallb und ingresse

### Volume Mounts

* taiga-static-data:
* taiga-media-data:
* taiga-db-data:
* taiga-async-rabbitmq-data:
* taiga-events-rabbitmq-data:

### Secrets

* admin user?
* secret-key
* db
* email
* rabbit-mq

### Networking

We need to know what the "hostname" KW does in docker compose.
Then need to find a translation for this functionality to kubernetes.

ToDo: How do we direct traffic towards the frontend pod? 
Do we need to touch the frontend config regarding the default address (localhost:9000) of the API?

## Purpose

## Status

## Try out


## Usage

You need:

...

* and a kubernetes cluster provisioned by [provs]

...
Let c4k-taiga generate your .yaml file.  
Apply this file on your cluster with `kubectl apply -f yourApp.yaml`.  
Done.

### resource requests and limits

You may want to adjust the resource requests and limits of the build and init containers to your specific scenario.

## Development & mirrors

Development happens at: https://repo.prod.meissa.de/meissa/c4k-taiga

Mirrors are:

* https://gitlab.com/domaindrivenarchitecture/c4k-taiga (issues and PR, CI)
* https://github.com/DomainDrivenArchitecture/c4k-taiga

For more details about our repository model see: https://repo.prod.meissa.de/meissa/federate-your-repos

## License

Copyright © 2022 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)

[provs]: https://gitlab.com/domaindrivenarchitecture/provs/
