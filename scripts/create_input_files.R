# Check for one argument
if (length(commandArgs(trailingOnly = TRUE)) != 1) {
  stop("Usage: create_input_files.R <full_directory_path>")
}

library(Matrix)


# Get experiment directory path
experiment_directory_path <- commandArgs(trailingOnly = TRUE)[1]
rds_matrix_path <- paste0(experiment_directory_path, "/matrix.rds")

rds_matrix <- readRDS(rds_matrix_path)
t_rds_matrix <- t(rds_matrix)

dense_gene_matrix <- as.matrix(t_rds_matrix)
dense_cell_matrix <- as.matrix(rds_matrix)

genecodes <- data.frame(genecode = rownames(dense_gene_matrix), row.names = NULL)
barcodes <- data.frame(barcode = rownames(dense_cell_matrix), row.names = NULL)

gene_matrix_w_codes <- cbind(genecodes, dense_gene_matrix)
cell_matrix_w_codes <- cbind(barcodes, dense_cell_matrix)

write.csv(gene_matrix_w_codes, paste0(experiment_directory_path, "/gene_matrix.csv"), row.names = FALSE)
write.csv(cell_matrix_w_codes, paste0(experiment_directory_path, "/cell_matrix.csv"), row.names = FALSE)

