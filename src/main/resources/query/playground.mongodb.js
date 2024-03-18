use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = ObjectId("65f83b724a88615d5c41735a")
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa"];
result = db.getCollection("geneExpressionList").aggregate([
    {
      $match: {
        "experiment.$id": experimentId,
        "code": { $in: geneCodes }
      }
    },
    {
      $project: {
        experimentId: "$experiment.$id",
        code: "$code",
        expressions:1
        
      }
    }
    
  ])/*
/* */
print(result);
