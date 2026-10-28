// package com.mansa.architecture;


// import com.tngtech.archunit.core.domain.JavaClasses;
// import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.core.importer.ImportOption;
// import com.tngtech.archunit.lang.ArchRule;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;

// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
// import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

// class ArchitectureTest {

//     private static JavaClasses classes;

//     @BeforeAll
//     static void loadClasses() {
//         classes = new ClassFileImporter()
//                 .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
//                 .importPackages("com.bankpayx.mobilemoney");
//     }

//     @Test
//     void domainShouldNotDependOnInfrastructure() {
//         ArchRule rule = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat()
//                 .resideInAPackage("..infrastructure..");

//         rule.check(classes);
//     }

//     @Test
//     void domainShouldNotDependOnApplication() {
//         ArchRule rule = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat()
//                 .resideInAPackage("..application..");

//         rule.check(classes);
//     }

//     @Test
//     void domainShouldNotDependOnSpring() {
//         ArchRule rule = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat()
//                 .resideInAPackage("org.springframework..");

//         rule.check(classes);
//     }

//     @Test
//     void applicationShouldNotDependOnInfrastructure() {
//         ArchRule rule = noClasses()
//                 .that().resideInAPackage("..application..")
//                 .and().resideOutsideOfPackage("..application..service..")
//                 .should().dependOnClassesThat()
//                 .resideInAPackage("..infrastructure..");

//         rule.check(classes);
//     }

//     @Test
//     void applicationShouldNotDependOnApi() {
//         ArchRule rule = noClasses()
//                 .that().resideInAPackage("..application..")
//                 .should().dependOnClassesThat()
//                 .resideInAPackage("..api..");

//         rule.check(classes);
//     }

//     @Test
//     void controllersShouldOnlyDependOnApplicationPorts() {
//         ArchRule rule = classes()
//                 .that().resideInAPackage("..api.controller..")
//                 .should().onlyDependOnClassesThat()
//                 .resideInAnyPackage(
//                         "..api..",
//                         "..application.port.in..",
//                         "..application.usecase..",
//                         "..domain.valueobject..",
//                         "..infrastructure.monitoring..",
//                         "..infrastructure.security..",
//                         "com.fasterxml.jackson..",
//                         "jakarta..",
//                         "org.springframework..",
//                         "java..",
//                         "org.slf4j.."
//                 );

//         rule.check(classes);
//     }

//     @Test
//     void operatorAdaptersShouldImplementMobileMoneyOperatorPort() {
//         ArchRule rule = classes()
//                 .that().resideInAPackage("..infrastructure.operator..")
//                 .and().haveSimpleNameEndingWith("Adapter")
//                 .should().implement(
//                         com.bankpayx.mobilemoney.application.port.out.MobileMoneyOperatorPort.class
//                 );

//         rule.check(classes);
//     }

//     @Test
//     void persistenceAdaptersShouldImplementRepositoryPorts() {
//         ArchRule rule = classes()
//                 .that().resideInAPackage("..infrastructure.persistence.adapter..")
//                 .should().beAnnotatedWith(org.springframework.stereotype.Component.class);

//         rule.check(classes);
//     }

//     @Test
//     void valueObjectsShouldBeRecordsOrFinal() {
//         ArchRule rule = classes()
//                 .that().resideInAPackage("..domain.valueobject..")
//                 .and().areNotEnums()
//                 .should().beRecords()
//                 .orShould().haveModifier(com.tngtech.archunit.core.domain.JavaModifier.FINAL);

//         rule.check(classes);
//     }

//     @Test
//     void layeredArchitectureShouldBeRespected() {
//         ArchRule rule = layeredArchitecture()
//                 .consideringAllDependencies()
//                 .layer("API").definedBy("..api..")
//                 .layer("Application").definedBy("..application..")
//                 .layer("Domain").definedBy("..domain..")
//                 .layer("Infrastructure").definedBy("..infrastructure..")
//                 .layer("Config").definedBy("..config..")

//                 .whereLayer("API").mayOnlyAccessLayers("Application", "Domain", "Infrastructure", "Config")
//                 .whereLayer("Application").mayOnlyAccessLayers("Domain")
//                 .whereLayer("Domain").mayNotAccessAnyLayer()
//                 .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain")
//                 .whereLayer("Config").mayOnlyAccessLayers("Application", "Domain", "Infrastructure");

//         rule.check(classes);
//     }
// }
