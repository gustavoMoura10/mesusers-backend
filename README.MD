# Mesusers-Backend

Projeto backend de consumo de usuários para execução

## Dependencias iniciais
- Baixe a versão LTS do Java (https://www.oracle.com/br/java/technologies/downloads/#jdk21-windows)
- Baixe o Git em seu computador (https://git-scm.com/downloads)
- Baixe a versão LTS do Docker (https://docs.docker.com/)
- Baixe a versão LTS do Maven (https://maven.apache.org/download.cgi)
- Em seguida clone o projeto em seu computador.
- Com um terminal, vá até o caminho da pasta e digite npm
```bash
  docker compose up -d
```


## Criar arquivo de application properties
- Dentro da pasta src/main, crie uma pasta chamada resourcer
- Crie um arquivo com nome application.properties, application-dev.properties,application-test.properties
- No application-dev.properties e application-test.properties colocar as seguintes informações
```
//5433 para teste
espring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.enable_lazy_load_of_transitive_objects=true
jwt.secret.key=umaSenhaSecretaParaTestes

```
- application.properties
```

spring.profiles.active=@activeProfile@

```
## Baixando dependências do POM

- Após configurar tudo, executar o comando maven no terminal dentro da pasta principal.

```bash
  mvn dependency:resolve
```
- Depois executar 

```bash
  mvn spring-boot:run
```
# Pronto, servidor de pé 😉
