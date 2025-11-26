import { Router } from "express";
import { ClienteController } from "../controllers/clienteController";

const router = Router();
const clienteController = new ClienteController();

router.get('/', clienteController.getAll.bind(clienteController));
router.get('/:id', clienteController.getById.bind(clienteController));
router.post('/', clienteController.create.bind(clienteController));
router.put('/:id', clienteController.update.bind(clienteController));
router.delete('/:id', clienteController.delete.bind(clienteController));

export default router;
