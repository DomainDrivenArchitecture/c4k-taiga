# convention 4 kubernetes: c4k-taiga

[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-taiga.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-taiga) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-taiga/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-taiga/-/commits/main) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [taiga & Blog](https://domaindrivenarchitecture.org)

## Requirements

https://github.com/kaleidos-ventures/taiga-docker
https://community.taiga.io/t/taiga-30min-setup/170

* Docker
    * eigener image build, da eigene Konfiguration?
* Postgres
* Nginx
    * Mit config

## Purpose

## Status

## Try out

Click on the image to try out in your browser:

[![Try it out](doc/tryItOut.png "Try out yourself")](https://domaindrivenarchitecture.org/pages/dda-provision/c4k-taiga/)

Your input will stay in your browser. No server interaction is required.

You will also be able to try out on cli:
```
target/graalvm/c4k-taiga src/test/resources/taiga-test/valid-config.yaml src/test/resources/taiga-test/valid-auth.yaml | kubeval -
target/graalvm/c4k-taiga src/test/resources/taiga-test/valid-config.yaml src/test/resources/taiga-test/valid-auth.yaml | kubectl apply -f -
```


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

* https://gitlab.com/domaindrivenarchitecture/c4k-website (issues and PR, CI)

For more details about our repository model see: https://repo.prod.meissa.de/meissa/federate-your-repos

## License

Copyright © 2022 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)

[provs]: https://gitlab.com/domaindrivenarchitecture/provs/
