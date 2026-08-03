# Movies Dataset - Truth Discovery Comparative Analysis

## Visão Geral

Este documento descreve como o dataset de filmes (`dataset_full.csv`) foi integrado ao Truth Discovery Comparative Analysis para testar a capacidade dos algoritmos em cenários de **múltiplas verdades**.

## Motivação

O trabalho original do Truth Discovery Comparative Analysis focava em cenários de **verdade única**, onde existe apenas um valor correto para cada atributo. Este novo dataset de filmes foi criado para testar os mesmos algoritmos em um cenário mais realista onde:

- **Múltiplas fontes de dados**: OMDB e Wikidata possuem informações sobre o mesmo filme
- **Informações conflitantes**: As fontes podem ter valores diferentes para o mesmo atributo
- **Informações ausentes**: Nem todas as fontes têm informações completas para todos os filmes

## Estrutura do Dataset

### Dados Utilizados

O dataset de filmes contém informações sobre ~1000 filmes com os seguintes atributos:

- **tconst**: Identificador único do filme (IMDb ID)
- **title**: Título do filme
- **directors**: Diretores do filme
- **writers**: Roteiristas do filme

Cada atributo possui informações de duas fontes:

1. **OMDB**: Open Movie Database - dados estruturados e mais completos
2. **Wikidata**: Base de dados colaborativa - dados potencialmente conflitantes

### Arquivos Criados

#### 1. Claims 
Localização: `DAFNAData/formatted/movies/claims/`

Dois arquivos no formato TSV:
- `omdb.txt` - Claims da fonte OMDB (3,456 claims)
- `wikidata.txt` - Claims da fonte Wikidata (1,143 claims)

**Formato de cada linha:**
```
claimId | objectId | propertyName | propertyValue | sourceId | timestamp | (vazio)
50000   | tt0003442 | title | Tess of the D'Urbervilles | omdb | 2026-07-28 |
```

#### 2. Verdade (Ground Truth)
Localização: `DAFNAData/formatted/movies/truth/movies-truth.txt`

Arquivo TSV com os valores de verdade para comparação dos algoritmos.

**Formato:**
```
objectId	propertyName	propertyValue
tt0003442	title	Tess of the D'Urbervilles
tt0003442	directors	J. Searle Dawley
```

**Nota**: Os valores de verdade foram extraídos da OMDB, que é geralmente mais confiável para dados de filmes.

## Como Usar

### 1. Processar o Dataset

Execute o seguinte comando para processar o dataset de filmes:

```bash
cd /home/thilons/Documentos/tcc/multi_truth_discovery/Truth_Discovery_Comparative_Analysis
java -cp build/classes main.MainClass movies
```

### 2. Comparar Métodos

Para executar os algoritmos de Truth Discovery no dataset de filmes:

```bash
# O runner de métricas agora inclui o dataset movies.
./compare_metrics.sh
```

Observação: `compare_metrics.sh` compara os datasets legados e também o novo dataset `movies`, usando as claims OMDB + Wikidata unificadas contra o arquivo de truth gerado a partir de OMDB.

### 3. Análise de Resultados

Os resultados serão salvos em:
- `DAFNAData/experiments/datasetLog/` - Logs dos datasets
- `DAFNAData/experiments/voterLog/` - Logs dos votantes (algoritmos)
- `DAFNAData/experiments/charts/chartsCollection/all/` - Gráficos comparativos

## Métricas Esperadas

Os algoritmos de Truth Discovery serão avaliados usando:

- **Acurácia**: Percentual de valores corretamente identificados como verdade
- **Precisão**: Entre os valores identificados como verdade, quantos estão corretos
- **Recall**: Quantos dos valores verdadeiros foram identificados corretamente
- **F1-Score**: Média harmônica entre precisão e recall

## Estrutura de Diretórios

```
Truth_Discovery_Comparative_Analysis/
├── DAFNAData/
│   ├── unformatted/
│   │   └── movies/           # Diretório de entrada (vazio, dados já processados)
│   ├── formatted/
│   │   └── movies/
│   │       ├── claims/       # Claims das fontes
│   │       │   ├── omdb.txt
│   │       │   └── wikidata.txt
│   │       └── truth/        # Arquivo de verdade
│   │           └── movies-truth.txt
│   └── experiments/
│       ├── datasetLog/       # Logs de processamento
│       ├── voterLog/         # Logs dos algoritmos
│       └── charts/           # Gráficos gerados
```

## Scripts de Conversão

Dois scripts Python foram criados para gerar os arquivos do dataset:

### 1. `convert_movies_dataset.py`
Converte `dataset_full.csv` em arquivos de claims formatados:
```bash
python3 convert_movies_dataset.py
```

Gera:
- `DAFNAData/formatted/movies/claims/omdb.txt`
- `DAFNAData/formatted/movies/claims/wikidata.txt`

### 2. `create_movies_truth.py`
Cria o arquivo de verdade baseado nos dados OMDB:
```bash
python3 create_movies_truth.py
```

Gera:
- `DAFNAData/formatted/movies/truth/movies-truth.txt`

## Implementação Java

### Classe Principal
- **Arquivo**: `source/java-code/qcri/dafna/dataModel/dataSet/dataSetFormatter/MovieDataSetReadOldWriteNew.java`
- **Métodos**:
  - `readMoviesClaimsAndWriteTruthFiles()` - Processa os arquivos de claims e verdade
  - `readClaimsFile()` - Lê arquivo de claims
  - `readTruthFile()` - Lê arquivo de verdade

### Configuração Global
- **Arquivo**: `source/java-code/qcri/dafna/dataModel/data/Globals.java`
- **Constantes adicionadas**:
  - `directory_UnformattedMoviesFiles`
  - `directory_formattedDAFNADataset_MoviesFolder`
  - `directory_formattedDAFNADataset_MoviesClaimsFolder`
  - `directory_formattedDAFNADataset_MoviesTruthFolder`
  - `movieDataSet_Title`, `movieDataSet_Directors`, `movieDataSet_Writers`

### MainClass
- **Arquivo**: `source/java-code/main/MainClass.java`
- **Novo modo**: `java -cp build/classes main.MainClass movies`

## Características do Dataset

### Conflitos Detectados

O dataset possui vários tipos de conflitos interessantes:

1. **Variações Ortográficas**: Ex. "Émile Chautard" vs "Emile Chautard"
2. **Nomes de Diretores/Escritores**: Diferentes formatos e transliterações
3. **Dados Incompletos**: Wikidata tem muitos campos vazios
4. **Erros de Digitação**: Pequenas diferenças que podem indicar incompletude

### Estatísticas

- **Total de filmes**: ~1000
- **Claims OMDB**: 3,456
- **Claims Wikidata**: 1,143
- **Total de claims**: 4,599
- **Atributos por filme**: 3 (title, directors, writers)
- **Cobertura média de Wikidata**: ~33% dos claims OMDB

## Próximos Passos

1. **Executar os algoritmos**: Rodar os algoritmos de Truth Discovery no novo dataset
2. **Analisar resultados**: Comparar performance em cenários de múltiplas verdades
3. **Otimizar parâmetros**: Ajustar configurações dos algoritmos para melhor performance
4. **Documentar descobertas**: Registrar insights sobre como múltiplas fontes afetam a descoberta de verdade

## Referências

- **Dataset Original**: `/home/thilons/Documentos/tcc/multi_truth_discovery/Datasets/joined/dataset_full.csv`
- **Truth Discovery Paper**: [Arxiv CoRR abs/1409.6428](https://arxiv.org/abs/1409.6428)
- **Estrutura Original**: Baseada em Weather, Population, Flight, Biography datasets

## Autor

Integração do dataset de filmes realizada em 28 de Julho de 2026.
