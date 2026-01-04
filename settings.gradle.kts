rootProject.name = "rider-templater"

include(":core")
include(":common-ui")
include(":cli")
include(":plugin")
include(":ksp-codegen")

project(":core").projectDir = file("src/core")
project(":common-ui").projectDir = file("src/common-ui")
project(":cli").projectDir = file("src/cli")
project(":plugin").projectDir = file("src/plugin")
project(":ksp-codegen").projectDir = file("src/ksp-codegen")
