# Deterministic-Agent-Based-Model

![](Agent-Based Disease Model 2020-09-05 13-49-36_1.gif)
Disease spread induced by a contact network generated from University of North Texas class enrollment data.

Stochastic experimental results can be reproduced by using "743" as the SplittableRandom seed and discarding non-outbreak trials (<20% recovered). Results using the Partial Infection Model can be reproduced by enabling first order backflow correction in the function "runTrickleDay()" of the StaticSimulation class. Both sets of experiments were run with vertices.get(0) as the seed of infection.
