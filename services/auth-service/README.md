Ajouter une clé secrète dans un fichier .env et l'utiliser dans un fichier application.yml (généralement pour une application Spring Boot) est une bonne pratique de sécurité pour éviter de "hardcoder" des informations sensibles.
Voici les étapes clés :
1. Créer le fichier .env
À la racine de votre projet, créez un fichier nommé .env.
Ajoutez votre secret au format CLE=valeur. 
SerpApi
SerpApi
 +1
env
# .env file
API_SECRET_KEY=supersecret12345
DB_PASSWORD=monpassword
Utilisez le code avec précaution.
2. Sécuriser le fichier .env (Indispensable)
Ajoutez .env à votre fichier .gitignore pour éviter de pousser vos clés secrètes sur Git. 
Codefinity
Codefinity
gitignore
# .gitignore
.env
Utilisez le code avec précaution.
3. Charger le fichier .env dans Spring Boot
Spring Boot ne lit pas nativement les fichiers .env. Il faut utiliser une dépendance pour charger ces variables, par exemple spring-dotenv ou utiliser une configuration locale. 
KDnuggets
KDnuggets
 +1
Méthode recommandée : Ajouter une dépendance dans votre pom.xml pour charger automatiquement le fichier .env au démarrage. 
KDnuggets
KDnuggets
    xml
    <dependency>
        <groupId>me.paulschwarz</groupId>
        <artifactId>spring-dotenv</artifactId>
        <version>4.0.0</version>
    </dependency>
Utilisez le code avec précaution.

4. Utiliser la variable dans application.yml 
Dans votre fichier src/main/resources/application.yml, utilisez la syntaxe ${NOM_DE_LA_VARIABLE} pour référencer la valeur du fichier .env. 
Baeldung
Baeldung
 
yaml
# application.yml
my:
  api:
    key: ${API_SECRET_KEY}
  database:
    password: ${DB_PASSWORD:default_password} # "default_password" est utilisé si la variable n'existe pas
Utilisez le code avec précaution.
5. Utiliser la valeur dans le code (Java)
Vous pouvez maintenant injecter cette propriété dans vos classes Java en utilisant @Value. 
''' code ''
java
@Value("${my.api.key}")
private String apiSecretKey;

'''fin'''
Utilisez le code avec précaution.
Résumé des bonnes pratiques
Ne jamais commit le fichier .env.
Utilisez des valeurs par défaut dans .yml uniquement pour les environnements de dev non-sensibles.
Sur un serveur de prod, configurez directement les variables d'environnement dans la CI/CD (GitHub Actions, GitLab CI) au lieu d'utiliser un .env.