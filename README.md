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

### Docker Compose (DC) -> Kubernetes

We implemented a deployment and service in kubernetes for each DC Service.
Configmaps and secrets were implemented, to avoid redundancy and readability also to increase security a bit.
For all volumes described in DC we implemented PVCs and volume refs.

A config.py (used for taiga-back ) was introduced for reference.
A config.json (used for taiga-front ) was introduced for reference.
NB: It might be necessary to actually map both from a config map to their respective locations in taiga-back and taiga-front. Description for that is [here](https://docs.taiga.io/setup-production.html).
A mix of both env-vars and config.py in one container is not possible.

#### depends_on

We currently assume, that it will work without explicitly defining a startup order.

#### DC Networking

https://github.com/compose-spec/compose-spec/blob/master/spec.md

The `hostname` KW sets the hostname of a container.
It should have no effect on the discoverability of the container in kubernetes.

The `networks` KW defines the networks that service containers are attached to, referencing entries under the top-level networks key.
This should be taken care of by our kubernetes installation.

#### Pod to Pod Possible Communications

Taiga containers that need to reach other taiga containers:
taiga-async -> taiga-async-rabbitmq
taiga-events -> taiga-events-rabbitmq
This is not quite clear, but probably solved with the implementation of services.

### Init container

Es gibt einen Init-Container mit namen *taiga-manage* im deployment.
Dieser erstellt einen Admin User mit credentials aus dem taiga-back-secret.

#### Einen admin-user anlegen

https://github.com/kaleidos-ventures/taiga-docker#configure-an-admin-user

folglich:  

https://docs.djangoproject.com/en/4.2/ref/django-admin/#django-admin-createsuperuser

Also DJANGO_SUPERUSER_TAIGAADMIN und DJANGO_SUPERUSER_PASSWORD
sollten für den Container gesetzt sein.

Dann noch ein run befehl mit: python manage.py createsuperuser im init container unterbringen.

### Deployments

Separate deployments exist for each of the taiga modules:

Taiga-back reads many values in config.py from env vars as can be seen in the taiga-back [config.py](
https://github.com/kaleidos-ventures/taiga-back/blob/main/docker/config.py). These are read from configmaps and secrets in the deployment.

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
