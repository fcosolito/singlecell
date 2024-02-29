use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = "exp1cluster_0.101"
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1"];
db.getCollection("cellExpressionList").aggregate([
    {
        $match:{
            experimentId: experimentId,
            geneCode: { $in: geneCodes}
        }
    },
    {
        $unwind: "$expressions"
    },
    {
        $group: {
          _id: "$expressions.cellId",
          sumOfExpressions: {
            $sum: "$expressions.expression"
          }
        }
    }
])/*
  
/* */