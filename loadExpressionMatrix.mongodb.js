
use("singlecell");

experimentId = "exp1"

db.getCollection("experiment").aggregate([
    {
        $match: {
          _id: experimentId
        }
    },
    {
        $unwind: "$cells"
    },
    {
        $lookup: {
            from: "cell",
            localField: "cells",
            foreignField: "_id",
            as: "cellInfo"
        }
    },
    {
        $unwind: "$cellInfo"
    },
    {
        $lookup: {
          from: "geneExpression",
          localField: "cellInfo.geneExpressions",
          foreignField: "_id",
          as: "expressions"
        }

    },
    {
        $project: {
            experimentId: "$_id",
            cellId: "$cellInfo._id",
            expressions: "$expressions"
        }

    },
    {
        $group: {
            _id: "$experimentId",
            cells: {$push: "$cellId"},
            expressions: {$push: "$expressions"}
        }
    },


])