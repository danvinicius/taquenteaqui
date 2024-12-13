# Ta Quente Aqui - Projeto para disciplina de Sistemas Distribuídos

## Descrição

O Ta Quente Aqui é uma aplicação desenvolvida em Kotlin para a denúncia de incêndios. Os usuários podem registrar suas denúncias incluindo uma descrição, uma foto e sua geolocalização automaticamente capturada pelo dispositivo. Outros usuários podem visualizar as denúncias em um mapa interativo.

<br>

## Funcionalidades

- Cadastro de Denúncias:

  - Descrição detalhada do incidente

  - Upload de uma foto ilustrativa

  - Geolocalização automática usando GPS

- Visualização de Denúncias:

  - Mapa interativo exibindo a localização das denúncias

  - Detalhes completos ao clicar nos marcadores do mapa
  
<br>

## Tecnologias Utilizadas

- Linguagem: Kotlin

- Plataforma: Android

- Serviços Utilizados:

- Google Maps API: para exibição das denúncias no mapa

- HiveMQ: para publicação e subscrição das mensagens entre os usuários
  
<br>

## Como Funciona

O usuário registra uma denúncia preenchendo uma descrição e anexando uma foto. A localização geográfica é obtida automaticamente usando a API de localização do dispositivo.
As informações são enviadas ao servidor usando o HiveMQ. Outros usuários recebem as denúncias em tempo real e podem visualizá-las no mapa.

<br>

## Como Configurar e Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/danvinicius/taquenteaqui.git
   ```
2. Abra o projeto no Android Studio.
3. Configure a API do Google Maps e o servidor HiveMQ nas chaves apropriadas.
4. Compile e execute o aplicativo em um dispositivo Android ou emulador.
   
<br>

## Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests no repositório do projeto.

<br>

## Licença
Este projeto é licenciado sob a MIT License.

