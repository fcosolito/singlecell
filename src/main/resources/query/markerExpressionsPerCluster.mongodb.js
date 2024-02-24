use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = "exp1cluster_0.101"
markerList = ["Vcpip1", "Tram1", "Gata1","Ptma", "Tubab1"]
db.getCollection("cell").aggregate([
    {
        $match:{
            clusterIds: clusterId
        }
    },
    {
        $lookup: {
          from: "geneExpressionList",
          localField: "geneExpressionId",
          foreignField: "_id",
          as: "expressionInfo"
        }
    },
    {
        $unwind: "$expressionInfo"
    },
    {
        $project: {
            barcode:1,
            expressions: {
                $filter: {
                    input: "$expressionInfo.geneExpressions",
                    as: "geneExp",
                    cond: { $in: ["$$geneExp.geneCode", markerList]}
                }
          }
        }
    }
    
])/*
/* */