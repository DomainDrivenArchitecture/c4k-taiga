FROM clojure:lein

# Prepare Entrypoint Script
ADD resources /tmp

ENV BUILDDIR="/etc/website"
ENV SOURCEDIR="/etc/websitesource"
ENV WEBSITEROOT="/var/www/html/website/"
ENV HASHFILEDIR="/var/hashfile.d"

RUN /tmp/install.sh
