# Rastreador Espacial

O **Rastreador Espacial** é uma aplicação em Java para rastrear e avaliar o risco de objetos espaciais e visualizar fenômenos astronômicos através do consumo de APIs públicas da NASA (NeoWs e APOD) e Celestrak com suporte a cache local.

---

## 🔑 Obtenção da Chave de API da NASA

Para utilizar as APIs da NASA sem limites restritivos de requisição, você pode obter uma chave gratuita da seguinte forma:

1. Acesse o portal [NASA Open APIs](https://api.nasa.gov/).
2. Preencha o formulário de cadastro gratuito ("Generate API Key") com seu nome e e-mail.
3. Copie a chave de API fornecida.
4. Crie um arquivo `.env` na raiz do projeto (ou copie a partir de `.env.example`) e configure sua chave:
   ```env
   NASA_API_KEY=sua_chave_aqui
   ```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 25 ou superior
- Apache Maven 3.8+

### Compilar o projeto
```bash
mvn clean compile
```

### Executar os testes
```bash
mvn test
```

### Executar a aplicação
```bash
mvn compile exec:java
```

