# Zone batching

The client is optimised to prevent sending repeated coordinate information and to reuse encoded updates packets by batching updates for floor items, objects, projects and some graphics, per zone.

 
Zone packet sends zone location relative to the map zone size and plane