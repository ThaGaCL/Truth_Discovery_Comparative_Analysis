#!/usr/bin/env python3
"""
Script para criar arquivo de verdade (truth file) para dataset de filmes.

O arquivo de verdade contém as informações corretas dos filmes.
Estratégia: usar os valores OMDB como verdade (por ser mais completo).
"""

import csv
import os

DATASET_PATH = "../Datasets/joined/dataset_full.csv"
TRUTH_OUTPUT = "DAFNAData/formatted/movies/truth/movies-truth.txt"

def create_truth_file():
    """Cria arquivo de verdade baseado no dataset OMDB."""
    print(f"Criando arquivo de verdade: {TRUTH_OUTPUT}")
    
    if not os.path.exists(DATASET_PATH):
        print(f"Erro: Arquivo {DATASET_PATH} não encontrado!")
        return False
    
    try:
        # Criar diretório se não existir
        os.makedirs(os.path.dirname(TRUTH_OUTPUT), exist_ok=True)
        
        with open(DATASET_PATH, 'r', encoding='utf-8') as csvfile, \
             open(TRUTH_OUTPUT, 'w', encoding='utf-8') as truthfile:
            
            reader = csv.DictReader(csvfile)
            count = 0

            def atomic_values(raw_value):
                if not raw_value:
                    return []
                raw_value = raw_value.strip()
                if not raw_value or raw_value == "\\N":
                    return []
                return [part.strip() for part in raw_value.split(';') if part.strip() and part.strip() != "\\N"]
            
            for row in reader:
                tconst = row.get('tconst', '').strip()
                omdb_title = row.get('omdb_title', '').strip()
                omdb_directors = row.get('omdb_directors', '').strip()
                omdb_writers = row.get('omdb_writers', '').strip()
                
                if not tconst:
                    continue
                
                # Usar valores OMDB como verdade, ignorar valores vazios
                if omdb_title and omdb_title != "\\N":
                    truthfile.write(f"{tconst}\ttitle\t{omdb_title}\n")
                    count += 1
                
                for director in atomic_values(omdb_directors):
                    truthfile.write(f"{tconst}\tdirectors\t{director}\n")
                    count += 1
                
                for writer in atomic_values(omdb_writers):
                    truthfile.write(f"{tconst}\twriters\t{writer}\n")
                    count += 1
        
        print(f"Arquivo de verdade criado com sucesso!")
        print(f"  - Total de valores de verdade: {count}")
        return True
    
    except Exception as e:
        print(f"Erro ao criar arquivo de verdade: {e}")
        return False

if __name__ == "__main__":
    import sys
    success = create_truth_file()
    sys.exit(0 if success else 1)
