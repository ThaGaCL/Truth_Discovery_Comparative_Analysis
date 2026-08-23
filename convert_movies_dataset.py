#!/usr/bin/env python3
"""
Script para converter dataset_full.csv em formato compatível com Truth Discovery Comparative Analysis.

Formato esperado:
claimId | objectIdentifier | propertyName | propertyValue | sourceId | timeStamp | (vazio)

Para datasets de filmes:
- objectIdentifier: tconst (identificador do filme)
- propertyName: title, directors, writers
- propertyValue: valor do atributo
- sourceId: tmdb, wikidata, cinemeta ou letterboxd
"""

import csv
import os
from pathlib import Path

# Configurações
DATASET_PATH = "../Datasets/normalized/4s_normalized_dataset_full.csv"
OUTPUT_DIR = "DAFNAData/formatted/movies/claims"
TIMESTAMP = "2026-08-03"
DELIMITER = "\t"

# IDs de claims - começar após o último ID de outros datasets
CLAIM_ID_START = 50000

class MovieDatasetConverter:
    def __init__(self, output_dir, delimiter="\t"):
        self.output_dir = output_dir
        self.delimiter = delimiter
        self.claim_id = CLAIM_ID_START
        
        # Criar writers para cada fonte
        self.tmdb_file = open(os.path.join(output_dir, "tmdb.txt"), "w", encoding="utf-8")
        self.wikidata_file = open(os.path.join(output_dir, "wikidata.txt"), "w", encoding="utf-8")
        self.cinemeta_file = open(os.path.join(output_dir, "cinemeta.txt"), "w", encoding="utf-8")
        self.letterboxd_file = open(os.path.join(output_dir, "letterboxd.txt"), "w", encoding="utf-8")
        
        self.tmdb_claims = 0
        self.wikidata_claims = 0
        self.cinemeta_claims = 0
        self.letterboxd_claims = 0

    def split_atomic_values(self, property_value):
        """Divide valores multi-valorados em claims atômicas."""
        if not property_value or property_value.strip() == "" or property_value == "\\N":
            return []

        values = []
        for part in property_value.split(";"):
            atomic_value = part.strip()
            if atomic_value and atomic_value != "\\N":
                values.append(atomic_value)
        return values
    
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
                    
                    # --- Processar claims TMDB ---
                    tmdb_title = row.get('tmdb_title', '')
                    tmdb_directors = row.get('tmdb_directors', '')
                    tmdb_writers = row.get('tmdb_writers', '')
                    
                    if self.write_claim(self.tmdb_file, tconst, "title", tmdb_title, "tmdb"):
                        self.tmdb_claims += 1
                    for director in self.split_atomic_values(tmdb_directors):
                        if self.write_claim(self.tmdb_file, tconst, "directors", director, "tmdb"):
                            self.tmdb_claims += 1
                    for writer in self.split_atomic_values(tmdb_writers):
                        if self.write_claim(self.tmdb_file, tconst, "writers", writer, "tmdb"):
                            self.tmdb_claims += 1
                    
                    # --- Processar claims Wikidata ---
                    wikidata_title = row.get('wikidata_title', '')
                    wikidata_directors = row.get('wikidata_directors', '')
                    wikidata_writers = row.get('wikidata_writers', '')
                    
                    if self.write_claim(self.wikidata_file, tconst, "title", wikidata_title, "wikidata"):
                        self.wikidata_claims += 1
                    for director in self.split_atomic_values(wikidata_directors):
                        if self.write_claim(self.wikidata_file, tconst, "directors", director, "wikidata"):
                            self.wikidata_claims += 1
                    for writer in self.split_atomic_values(wikidata_writers):
                        if self.write_claim(self.wikidata_file, tconst, "writers", writer, "wikidata"):
                            self.wikidata_claims += 1
                            
                    # --- Processar claims Cinemeta ---
                    cinemeta_title = row.get('cinemeta_title', '')
                    cinemeta_directors = row.get('cinemeta_directors', '')
                    cinemeta_writers = row.get('cinemeta_writers', '')
                    
                    if self.write_claim(self.cinemeta_file, tconst, "title", cinemeta_title, "cinemeta"):
                        self.cinemeta_claims += 1
                    for director in self.split_atomic_values(cinemeta_directors):
                        if self.write_claim(self.cinemeta_file, tconst, "directors", director, "cinemeta"):
                            self.cinemeta_claims += 1
                    for writer in self.split_atomic_values(cinemeta_writers):
                        if self.write_claim(self.cinemeta_file, tconst, "writers", writer, "cinemeta"):
                            self.cinemeta_claims += 1
                            
                    # --- Processar claims Letterboxd ---
                    letterboxd_title = row.get('letterboxd_title', '')
                    letterboxd_directors = row.get('letterboxd_directors', '')
                    letterboxd_writers = row.get('letterboxd_writers', '')
                    
                    if self.write_claim(self.letterboxd_file, tconst, "title", letterboxd_title, "letterboxd"):
                        self.letterboxd_claims += 1
                    for director in self.split_atomic_values(letterboxd_directors):
                        if self.write_claim(self.letterboxd_file, tconst, "directors", director, "letterboxd"):
                            self.letterboxd_claims += 1
                    for writer in self.split_atomic_values(letterboxd_writers):
                        if self.write_claim(self.letterboxd_file, tconst, "writers", writer, "letterboxd"):
                            self.letterboxd_claims += 1
                    
                    if row_num % 1000 == 0:
                        print(f"  Processados {row_num} filmes...")
            
            total_claims = self.tmdb_claims + self.wikidata_claims + self.cinemeta_claims + self.letterboxd_claims
            
            print(f"Dataset processado com sucesso!")
            print(f"  - Claims TMDB: {self.tmdb_claims}")
            print(f"  - Claims Wikidata: {self.wikidata_claims}")
            print(f"  - Claims Cinemeta: {self.cinemeta_claims}")
            print(f"  - Claims Letterboxd: {self.letterboxd_claims}")
            print(f"  - Total de claims: {total_claims}")
            print(f"  - Próximo ID de claim disponível: {self.claim_id}")
            
            return True
        
        except Exception as e:
            print(f"Erro ao processar dataset: {e}")
            return False
    
    def close(self):
        """Fecha os arquivos."""
        self.tmdb_file.close()
        self.wikidata_file.close()
        self.cinemeta_file.close()
        self.letterboxd_file.close()


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
        print(f"  - tmdb.txt")
        print(f"  - wikidata.txt")
        print(f"  - cinemeta.txt")
        print(f"  - letterboxd.txt")
    else:
        converter.close()
        print("Erro na conversão!")
        return False
    
    return True


if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)