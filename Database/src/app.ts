import express, { Express } from "express";
import {
	userRoutes,
	manejadorTokensRoutes,
	clienteRoutes,
	empleadoRoutes
} from './routes';

const app: Express = express();

app.use(express.json());

app.use('/db/users', userRoutes);
app.use('/db/tokens', manejadorTokensRoutes);
app.use('/db/clientes', clienteRoutes);
app.use('/db/empleados', empleadoRoutes);

export default app;