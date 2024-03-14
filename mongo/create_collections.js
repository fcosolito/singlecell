// Database to use
use("singlecell");

let collections = [
    {
        name: "project",
        indexes: [
        ]

    },
{
        name: "experiment",
        indexes: [
        ]

    },
{
        name: "sample",
        indexes: [
        ]

    },
{
        name: "cell",
        indexes: [
            {
                keys: {
                    "experiment.$id":1
                },
                options: {

                }
            },
        ]

    },
{
        name: "resolution",
        indexes: [
            {
                keys: {
                    "experiment.$id":1
                },
                options: {

                }
            },
        ]

    },
{
        name: "cluster",
        indexes: [
            {
                keys: {
                    "resolution.$id":1
                },
                options: {

                }
            },
        ]

    },
{
        name: "heatmapCluster",
        indexes: [
            {
                keys: {
                    "cluster.$id":1
                },
                options: {

                }
            },
        ]

    },
{
        name: "geneExpressionList",
        indexes: [
            {
                keys: {
                    "experiment":1,
                    "code":1
                },
                options: {
                    unique: true
                }
            },
            {
                keys: {
                    "experiment.$id":1,
                    "code":1
                },
                options: {
                }
            },
        ]

    },
{
        name: "cellExpressionList",
        indexes: [
            {
                keys: {
                    "cell":1
                },
                options: {
                    unique: true
                }
            },
            {
                keys: {
                    "cell.$id":1
                },
                options: {
                }
            },
        ]

    },
{
        name: "partialGeneExpressionList",
        indexes: [
            {
                keys: {
                    "code":1
                },
                options: {

                }
            },
        ]

    },
{
        name: "partialCellExpressionList",
        indexes: [
            {
                name: "cell_1",
                keys: {
                    "cell":1
                },
                options: {

                }
            },
        ]

    },
]

for (collection of collections){
    db.createCollection(collection.name, { autoIndexId: true });
    for (index of collection.indexes){
        db.getCollection(collection.name).createIndex(index.keys, index.options);
    }
}