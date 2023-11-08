from os import environ
from subprocess import run
from pybuilder.core import init, task
from ddadevops import *

default_task = "dev"
base_name = "taiga"
name = 'c4k-taiga'
MODULE = 'not-used'
PROJECT_ROOT_PATH = '.'

@init
def initialize(project):
    project.build_depends_on("ddadevops>=4.7.0")

    input = {
        "name": name,
        "module": MODULE,
        "stage": "notused",
        "project_root_path": PROJECT_ROOT_PATH,
        "build_types": [],
        "mixin_types": ["RELEASE"],
        "release_primary_build_file": "project.clj",
        "release_secondary_build_files": [
            "package.json",
            "infrastructure/backup/build.py",            
            ],
        "release_artifact_server_url": "https://repo.prod.meissa.de",
        "release_organisation": "meissa",
        "release_repository_name": name,
        "release_artifacts": [
            f"target/uberjar/{name}-standalone.jar",
            f"target/frontend-build/{name}.js",
        ],
        "release_main_branch": "main",
    }
    
    build = ReleaseMixin(project, input)
    build.initialize_build_dir()


@task
def test_clj(project):
    run("lein test", shell=True, check=True)


@task
def test_cljs(project):
    run("shadow-cljs compile test", shell=True, check=True)
    run("node target/node-tests.js", shell=True, check=True)


@task
def test_schema(project):
    run("lein uberjar", shell=True, check=True)
    run(
        f"java -jar target/uberjar/{name}-standalone.jar "
        + f"src/test/resources/{base_name}-test/valid-config.yaml "
        + f"src/test/resources/{base_name}-test/valid-auth.yaml | "
        + "kubeconform --kubernetes-version 1.23.0 --strict --skip Certificate -",
        shell=True,
        check=True,
    )


@task
def report_frontend(project):
    run("mkdir -p target/frontend-build", shell=True, check=True)
    run(
        "shadow-cljs run shadow.cljs.build-report frontend target/frontend-build/build-report.html",
        shell=True,
        check=True,
    )


@task
def package_frontend(project):
    run("mkdir -p target/frontend-build", shell=True, check=True)
    run("shadow-cljs release frontend", shell=True, check=True)
    run(
        f"cp public/js/main.js target/frontend-build/{name}.js",
        shell=True,
        check=True,
    )
    run(
        f"sha256sum target/frontend-build/{name}.js > target/frontend-build/{name}.js.sha256",
        shell=True,
        check=True,
    )
    run(
        f"sha512sum target/frontend-build/{name}.js > target/frontend-build/{name}.js.sha512",
        shell=True,
        check=True,
    )


@task
def package_uberjar(project):
    run(
        f"sha256sum target/uberjar/{name}-standalone.jar > target/uberjar/{name}-standalone.jar.sha256",
        shell=True,
        check=True,
    )
    run(
        f"sha512sum target/uberjar/{name}-standalone.jar > target/uberjar/{name}-standalone.jar.sha512",
        shell=True,
        check=True,
    )


@task
def upload_clj(project):
    run("lein deploy", shell=True, check=True)


@task
def lint(project):
    #run(
    #    "lein eastwood",
    #    shell=True,
    #    check=True,
    #)
    run(
        "lein ancient check",
        shell=True,
        check=True,
    )


@task
def patch(project):
    linttest(project, "PATCH")
    release(project)


@task
def minor(project):
    linttest(project, "MINOR")
    release(project)


@task
def major(project):
    linttest(project, "MAJOR")
    release(project)


@task
def dev(project):
    linttest(project, "NONE")


@task
def prepare(project):
    build = get_devops_build(project)
    build.prepare_release()


@task
def tag(project):
    build = get_devops_build(project)
    build.tag_bump_and_push_release()

@task
def publish_artifacts(project):
    build = get_devops_build(project)
    build.publish_artifacts()

def release(project):
    prepare(project)
    tag(project)


def linttest(project, release_type):
    build = get_devops_build(project)
    build.update_release_type(release_type)
    test_clj(project)
    test_cljs(project)
    test_schema(project)
    lint(project)
