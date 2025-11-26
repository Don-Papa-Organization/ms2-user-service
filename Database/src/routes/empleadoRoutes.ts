import { Router } from "express";
import { EmpleadoController } from "../controllers/empleadoController";

const router = Router();
const empleadoController = new EmpleadoController();

router.get('/cargo/:cargo', empleadoController.getByCargo.bind(empleadoController));
router.get('/', empleadoController.getAll.bind(empleadoController));
router.get('/:id', empleadoController.getById.bind(empleadoController));
router.post('/', empleadoController.create.bind(empleadoController));
router.put('/:id', empleadoController.update.bind(empleadoController));
router.delete('/:id', empleadoController.delete.bind(empleadoController));

export default router;
