#!/usr/bin/env python3
"""
Script para converter dataset_full.csv em formato compatível com Truth Discovery Comparative Analysis.

Formato esperado:
claimId | objectIdentifier | propertyName | propertyValue | sourceId | timeStamp | (vazio)

Para datasets de filmes:
- objectIdentifier: tconst (identificador do filme)
- propertyName: title, directors, writers
- propertyValue: valor do atributo
- sourceId: omdb ou wikidata
"""

import csv
import os
from pathlib import Path

# Configurações
DATASET_PATH = "../Datasets/joined/dataset_full.csv"
OUTPUT_DIR = "DAFNAData/formatted/movies/claims"
TIMESTAMP = "2026-07-28"
DELIMITER = "\t"

# IDs de claims - começar após o último ID de outros datasets
CLAIM_ID_START = 50000

class MovieDatasetConverter:
    def __init__(self, output_dir, delimiter="\t"):
        self.output_dir = output_dir
        self.delimiter = delimiter
        self.claim_id = CLAIM_ID_START
        
        # Criar writers para cada fonte
        self.omdb_file = open(os.path.join(output_dir, "omdb.txt"), "w", encoding="utf-8")
        self.wikidata_file = open(os.path.join(output_dir, "wikidata.txt"), "w", encoding="utf-8")
        
        self.omdb_claims = 0
        self.wikidata_claims = 0
    
    def write_claim(self, source_file, object_id, property_name, property_value, source_id):
        """Escreve uma claim no arquivo de claims."""
        if not property_value or property_value.strip() == "" or property_value == "\\N":
            return False
        
        # Remover espaços extras
        object_id = object_id.strip()
        property_value = property_value.strip()
        
        line = f"{self.claim_id}{self.delimiter}{object_id}{self.delimiter}{property_name}{self.delimiter}{property_value}{self.delimiter}{source_id}{self.delimiter}{TIMESTAMP}{self.delimiter}\n"
        
        try:
            source_file.write(line)
            self.claim_id += 1
            return True
        except Exception as e:
            print(f"Erro ao escrever claim: {e}")
            return False
    
    def process_dataset(self, csv_path):
        """Processa o dataset CSV."""
        print(f"Lendo dataset: {csv_path}")
        
        if not os.path.exists(csv_path):
            print(f"Erro: Arquivo {csv_path} não encontrado!")
            return False
        
        try:
            with open(csv_path, 'r', encoding='utf-8') as csvfile:
                reader = csv.DictReader(csvfile)
                
                for row_num, row in enumerate(reader, start=2):  # start=2 porque primeira linha é header
                    tconst = row.get('tconst', '').strip()
                    
                    if not tconst:
                        continue
                    
                    # Processar claims OMDB
                    omdb_title = row.get('omdb_title', '')
                    omdb_directors = row.get('omdb_directors', '')
                    omdb_writers = row.get('omdb_writers', '')
                    
                    if self.write_claim(self.omdb_file, tconst, "title", omdb_title, "omdb"):
                        self.omdb_claims += 1
                    if self.write_claim(self.omdb_file, tconst, "directors", omdb_directors, "omdb"):
                        self.omdb_claims += 1
                    if self.write_claim(self.omdb_file, tconst, "writers", omdb_writers, "omdb"):
                        self.omdb_claims += 1
                    
                    # Processar claims Wikidata
                    wikidata_title = row.get('wikidata_title', '')
                    wikidata_directors = row.get('wikidata_directors', '')
                    wikidata_writers = row.get('wikidata_writers', '')
                    
                    if self.write_claim(self.wikidata_file, tconst, "title", wikidata_title, "wikidata"):
                        self.wikidata_claims += 1
                    if self.write_claim(self.wikidata_file, tconst, "directors", wikidata_directors, "wikidata"):
                        self.wikidata_claims += 1
                    if self.write_claim(self.wikidata_file, tconst, "writers", wikidata_writers, "wikidata"):
                        self.wikidata_claims += 1
                    
                    if row_num % 1000 == 0:
                        print(f"  Processados {row_num} filmes...")
            
            print(f"Dataset processado com sucesso!")
            print(f"  - Claims OMDB: {self.omdb_claims}")
            print(f"  - Claims Wikidata: {self.wikidata_claims}")
            print(f"  - Total de claims: {self.omdb_claims + self.wikidata_claims}")
            print(f"  - Próximo ID de claim disponível: {self.claim_id}")
            
            return True
        
        except Exception as e:
            print(f"Erro ao processar dataset: {e}")
            return False
    
    def close(self):
        """Fecha os arquivos."""
        self.omdb_file.close()
        self.wikidata_file.close()


def main():
    # Verificar se o diretório de saída existe
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
        print(f"Diretório criado: {OUTPUT_DIR}")
    
    # Converter dataset
    converter = MovieDatasetConverter(OUTPUT_DIR, DELIMITER)
    
    if converter.process_dataset(DATASET_PATH):
        converter.close()
        print("\nConversão concluída com sucesso!")
        print(f"Arquivos gerados em: {OUTPUT_DIR}")
        print(f"  - omdb.txt")
        print(f"  - wikidata.txt")
    else:
        converter.close()
        print("Erro na conversão!")
        return False
    
    return True


if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)
