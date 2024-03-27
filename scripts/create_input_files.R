# Check for one argument
if (length(commandArgs(trailingOnly = TRUE)) != 1) {
  stop("Usage: create_input_files.R <full_directory_path>")
}

library(Matrix)


# Get experiment directory path
experiment_directory_path <- commandArgs(trailingOnly = TRUE)[1]
rds_matrix_path <- paste0(experiment_directory_path, "/matrix.rds")

print("Reading rds matrix file")
rds_matrix <- readRDS(rds_matrix_path)

print("Converting sparse matrix into dense matrix")
dense_cell_matrix <- as.matrix(rds_matrix)

print("Getting barcodes")
barcodes <- data.frame(barcode = rownames(dense_cell_matrix), row.names = NULL)

print("Binding barcodes to matrix")
cell_matrix_w_codes <- cbind(barcodes, dense_cell_matrix)

print("Writing cell matrix into file")
write.csv(cell_matrix_w_codes, paste0(experiment_directory_path, "/cell_matrix.csv"), row.names = FALSE)

print("Transposing matrix")
t_rds_matrix <- t(rds_matrix)

print("Converting sparse matrix into dense matrix")
dense_gene_matrix <- as.matrix(t_rds_matrix)

print("Getting genecodes")
genecodes <- data.frame(genecode = rownames(dense_gene_matrix), row.names = NULL)

print("Binding genecodes to matrix")
gene_matrix_w_codes <- cbind(genecodes, dense_gene_matrix)

print("Writing gene matrix into file")
write.csv(gene_matrix_w_codes, paste0(experiment_directory_path, "/gene_matrix.csv"), row.names = FALSE)
